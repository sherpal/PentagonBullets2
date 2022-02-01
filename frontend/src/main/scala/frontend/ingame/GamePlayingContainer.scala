package frontend.ingame

import assets.Asset
import boopickle.Default.Pickle
import com.raquo.laminar.api.L.*
import game.Keyboard
import game.Mouse
import game.ui.reactivepixi.ReactiveStage
import game.{GameStateManager, UserControls}
import gamecommunication.{ClientToServer, ServerToClient}
import gamelogic.entities.Entity
import gamelogic.gamestate.GameState
import models.menus.PlayerName
import org.scalajs.dom
import typings.pixiJs.PIXI.LoaderResource
import typings.pixiJs.mod.Application
import utils.domutils.ScalablyTypedScalaJSDomInterop.given
import utils.laminarzio.onMountZIOWithContext
import utils.laminarzio.Implicits._
import utils.websocket.Socket
import zio.duration.*
import zio.{UIO, ZIO}
import ziojs.Implicits.*
import gamecommunication.given_Pickler_GameState
import org.scalajs.dom.UIEvent
import org.scalajs.dom.raw.{Blob, FileReader}

import scala.scalajs.js.typedarray.TypedArrayBufferOps.*

object GamePlayingContainer {
  private val gameSceneSizeRatio = 1200 / 800.0

  def apply(
      playerName: PlayerName,
      timeDelta: Long,
      myEntityId: Entity.Id,
      application: Application,
      incomingMessages: EventStream[ServerToClient],
      messageWriter: Observer[ClientToServer],
      resources: PartialFunction[Asset, LoaderResource]
  ): HtmlElement = {

    val gameStateBus: EventBus[GameState] = new EventBus

    val gameStateSignal = gameStateBus.events.startWith(GameState.initialGameState)

    def addWindowResizeEventListener(stage: ReactiveStage) =
      for {
        window      <- UIO(dom.window)
        resizeQueue <- zio.Queue.unbounded[Unit]
        _ <- ZIO.effectTotal {
          window.addEventListener(
            "resize",
            (_: dom.Event) => frontend.runtime.unsafeRunToFuture(resizeQueue.offer(()))
          )
        }
        canvas <- UIO(stage.application.view)
        _ <- ZIO.effectTotal(frontend.runtime.unsafeRunToFuture((for {
          _ <- ZIO.sleep(500.millis)
          _ <- resizeQueue.take
          _ <- resizeQueue.takeAll // empty queue so that there is no buffering
          _ <- ZIO.effectTotal {
            val (canvasWidth, canvasHeight) = stage.computeApplicationViewDimension(
              window.innerWidth * 0.9,
              window.innerHeight * 0.9,
              gameSceneSizeRatio
            )
            canvas.width = canvasWidth.toInt
            canvas.height = canvasHeight.toInt
            stage.resize()
          }

        } yield ()).forever))
        _ <- resizeQueue.offer(()) // fixing the size at the beginning
      } yield ()

    def mountEffect(container: dom.Element)(implicit owner: Owner) = for {
      controls     <- services.localstorage.controls.retrieveControls
      _            <- UIO(println(controls)).whenInDev
      userControls <- UIO(new UserControls(new Keyboard(controls), new Mouse(application.view, controls)))
      stage        <- UIO(new ReactiveStage(application))
      _            <- addWindowResizeEventListener(stage)
      gameStateManager <- UIO(
        new GameStateManager(
          stage,
          GameState.initialGameState,
          incomingMessages.collect { case msg: ServerToClient.AddAndRemoveActions => msg },
          incomingMessages.collect { case msg: ServerToClient.BeginIn => msg },
          messageWriter,
          userControls,
          myEntityId,
          timeDelta,
          resources
        )
      )
      _ <- ZIO.effectTotal(gameStateManager.$gameStates.foreach(gs => gameStateBus.writer.onNext(gs)))
      _ <- ZIO.effectTotal(container.appendChild(application.view))
      _ <- ZIO.effectTotal(messageWriter.onNext(ClientToServer.ReadyToStart(playerName)))
    } yield ()

    div(
      onMountZIOWithContext(ctx => mountEffect(ctx.thisNode.ref)(ctx.owner)),
      div(
        display := "none",
        className := "gameStateSer",
        child <-- gameStateSignal.changes
          .throttle(33)
          .map(gameState => Pickle.intoBytes(gameState).arrayBuffer())
          .flatMap { buffer =>
            ZIO.effectAsync[Any, Nothing, String] { callback =>
              val blob   = new Blob(scalajs.js.Array(buffer))
              val reader = new FileReader()

              reader.onload = { (_: UIEvent) =>
                callback(UIO(reader.result.asInstanceOf[String]))
              }

              reader.readAsDataURL(blob)
            }
          }
          .map(dataUrl => dataUrl.drop("data:application/octet-stream;base64,".length))
          .map(base64String => div(base64String))
      )
    )
  }
}
