package frontend.ingame

import com.raquo.laminar.api.L.*
import frontend.AppState.{AnyAppState, GameStarted}
import gamecommunication.ClientToServer.Ping
import gamecommunication.ServerToClient.Pong
import gamecommunication.{ClientToServer, ServerToClient}
import models.menus.GameKeys.GameKey
import models.menus.PlayerName
import utils.websocket.BinaryWebSocket
import urldsl.language.dummyErrorImpl.*
import urldsl.vocabulary.{FromString, Printer}
import urldsl.errors.DummyError
import zio.*
import utils.laminarzio.Implicits.*
import game.synchronizeClock
import gamelogic.entities.Entity
import zio.duration.*

object GamePlaying {

  given Printer[GameKey] = Printer.factory(_.toString)

  given FromString[GameKey, DummyError] =
    FromString.factory(GameKey.fromString(_).left.map(_ => DummyError.dummyError))

  val socketQueryParam = param[String]("player-name") & param[GameKey]("game-key")

  def apply(gameStarted: GameStarted, stateChanger: Observer[AnyAppState]) = {

    def playerName: PlayerName = PlayerName(gameStarted.name.value)
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

    def synchronize(using Owner) = synchronizeClock(sendPing) zipParLeft zio.clock.sleep(2.second)

    div(
      "We are in!",
      ul(
        li("Connecting to server..."),
        child <-- socket.$open.map(_ => li("Connected. Synchronizing clocks...")),
        child <-- timeDeltaChanges.map(delta => li(s"Clock synchronized (delta: $delta). Loading assets.")),
        child <-- myEntityIdEvents.map(id => li(s"Received my entity id [$id]."))
      ),
      onMountCallback { context =>
        given Owner = context.owner
        socket.open()

        socket.$open.flatMap(_ => synchronize).foreach(delta => timeDelta.set(Some(delta.toLong)))
      },
      timeDeltaChanges.map(_ => ClientToServer.Ready) --> socket
    )
  }

}
