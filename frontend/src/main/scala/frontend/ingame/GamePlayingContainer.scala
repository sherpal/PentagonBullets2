package frontend.ingame

import assets.Asset
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
import utils.websocket.Socket
import zio.duration._
import zio.{UIO, ZIO}

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
      _            <- ZIO.unit
      controls     <- services.localstorage.controls.retrieveControls
      userControls <- UIO(new UserControls(new Keyboard(controls), new Mouse(application.view, controls)))
      stage        <- UIO(new ReactiveStage(application))
      _            <- addWindowResizeEventListener(stage)
      _ <- UIO(
        new GameStateManager(
          stage,
          GameState.initialGameState,
          incomingMessages.collect { case msg: ServerToClient.AddAndRemoveActions => msg },
          messageWriter,
          userControls,
          myEntityId,
          timeDelta,
          resources
        )
      )
      _ <- ZIO.effectTotal {
        container.appendChild(application.view)
      }
      _ <- ZIO.effectTotal(messageWriter.onNext(ClientToServer.ReadyToStart(playerName)))
    } yield ()

    div(
      s"This is the game playing container! ($playerName, $myEntityId, $timeDelta)",
      onMountZIOWithContext(ctx => mountEffect(ctx.thisNode.ref)(ctx.owner))
    )
  }
}
