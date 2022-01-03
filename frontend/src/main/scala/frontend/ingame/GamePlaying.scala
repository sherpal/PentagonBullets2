package frontend.ingame

import assets.{Asset, GameAssetLoader}
import com.raquo.laminar.api.L.*
import frontend.AppState.{AnyAppState, GameStarted}
import game.synchronizeClock
import gamecommunication.ClientToServer.Ping
import gamecommunication.ServerToClient.Pong
import gamecommunication.{ClientToServer, ServerToClient}
import gamelogic.entities.Entity
import models.menus.GameKeys.GameKey
import models.menus.PlayerName
import typings.pixiJs.PIXI.LoaderResource
import urldsl.errors.DummyError
import urldsl.language.dummyErrorImpl.*
import urldsl.vocabulary.{FromString, Printer}
import utils.laminarzio.Implicits.*
import utils.laminarzio.onMountZIO
import utils.websocket.BinaryWebSocket
import zio.*
import zio.duration.*
import typings.pixiJs.anon.Antialias as ApplicationOptions
import typings.pixiJs.mod.Application

import scala.scalajs.js.JSON

object GamePlaying {

  given Printer[GameKey] = Printer.factory(_.toString)

  given FromString[GameKey, DummyError] =
    FromString.factory(GameKey.fromString(_).left.map(_ => DummyError.dummyError))

  val socketQueryParam = param[String]("player-name") & param[GameKey]("game-key")

  def apply(gameStarted: GameStarted, stateChanger: Observer[AnyAppState]) = {

    val application: Application = new Application(
      ApplicationOptions()
        .setBackgroundColor(0x1099bb)
        .setWidth(1200)
        .setHeight(800)
    )
    val loader = new GameAssetLoader(application)

    val assetMap: Var[Option[PartialFunction[Asset, LoaderResource]]] = Var(Option.empty)

    val allAssetsLoaded = loader.endedLoadingEvent

    def playerName: PlayerName = gameStarted.playerName
    def gameKey: GameKey       = gameStarted.gameKey

    val timeDelta: Var[Option[Long]] = Var(Option.empty)

    val timeDeltaChanges: EventStream[Long] = timeDelta.signal.changes.collect { case Some(td) => td }

    val socket: BinaryWebSocket[ServerToClient, ClientToServer] =
      BinaryWebSocket(root / "in-game", socketQueryParam, (playerName.name, gameKey))

    val myEntityIdEvents: EventStream[Entity.Id] = socket.$in.collect { case ServerToClient.YourEntityIdIs(id) =>
      id
    }

    def sendPing(ping: Ping)(using Owner): UIO[Pong] = for {
      pongFiber <- ZIO
        .effectAsync[Any, Nothing, Pong](callback =>
          socket.$in.collect { case pong: Pong => pong }.map(UIO(_)).foreach(callback)
        )
        .fork
      _    <- ZIO.effectTotal(socket.outWriter.onNext(ping))
      pong <- pongFiber.join
    } yield pong

    def synchronize(using Owner) = synchronizeClock(sendPing, 100) zipParLeft zio.clock.sleep(2.second)

    div(
      "We are in!",
      ul(
        li("Connecting to server..."),
        child <-- socket.$error.map(event => li(s"Socket error! ${JSON.stringify(event)}")),
        child <-- socket.$open.map(_ => li("Connected. Synchronizing clocks...")),
        child <-- timeDeltaChanges.map(delta => li(s"Clock synchronized (delta: $delta).")),
        child <-- myEntityIdEvents.map(id => li(s"Received my entity id [$id].")),
        child <-- loader.$progressData.startWith(GameAssetLoader.initial).map { data =>
          val completion = scala.math.round(data.completion)
          val assetName  = data.assetName
          li(
            "Asset Loading Status:",
            Option.when(completion < 100)(progress(value := completion.toString)),
            s" ${completion.toInt}%, ($assetName)"
          )
        },
        child <-- allAssetsLoaded.map(_ => li("All assets have been properly loaded."))
      ),
      onMountCallback { context =>
        given Owner = context.owner
        socket.open()

        socket.$open.flatMap(_ => synchronize).foreach(delta => timeDelta.set(Some(delta.toLong)))
      },
      onMountZIO(
        loader.loadAssets.asSome.tap(assets => ZIO.effectTotal(assetMap.set(assets))).unit
      ),
      (timeDeltaChanges.combineWith(allAssetsLoaded)).map(_ => ClientToServer.Ready) --> socket
    )
  }

}
