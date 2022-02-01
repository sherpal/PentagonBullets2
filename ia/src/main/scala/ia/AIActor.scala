package ia

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import gamelogic.gamestate.GameState
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.interactions.Actions

import scala.concurrent.duration.*

object AIActor {
  sealed trait Command

  case class RefreshGameState(gameState: GameState) extends Command
  private case object Loop extends Command
  case object GameEnded extends Command

  def apply(driver: ChromeDriver): Behavior[Command] = Behaviors.setup[Command] { context =>

    context.log.info("AI actor setup.")

    Behaviors.receiveMessage {
      case RefreshGameState(gameState) =>
        context.log.info("First game state received, starting game loop...")
        context.self ! Loop
        receiver(gameState, initialState(driver))
      case _ =>
        Behaviors.unhandled
    }
  }

  private case class MouseAndKeyboardState(driver: ChromeDriver, mouseIsPressed: Boolean = false)

  private def initialState(driver: ChromeDriver) = MouseAndKeyboardState(driver)

  private def receiver(gameState: GameState, state: MouseAndKeyboardState): Behavior[Command] = Behaviors.receive {
    (context, command) =>
      command match {
        case RefreshGameState(newGameState) =>
          receiver(newGameState, state)
        case Loop if gameState.isPlaying =>
          context.scheduleOnce(1.second, context.self, Loop)
          context.log.info(s"AI Game loop! (${gameState.players.size} players alive)")

          val myId = gameState.players.keys.min

          if !state.mouseIsPressed && gameState.players.get(myId).exists(_.energy > 70) then {
            val canvas = state.driver.findElement(By.tagName("canvas"))

            new Actions(state.driver).moveToElement(canvas).moveByOffset(50, 50).clickAndHold().perform()

            receiver(gameState, state.copy(mouseIsPressed = true))
          } else if state.mouseIsPressed && gameState.players.get(myId).exists(_.energy < 50) then {
            new Actions(state.driver).release().perform()
            receiver(gameState, state.copy(mouseIsPressed = false))
          } else Behaviors.same
        case Loop =>
          context.scheduleOnce(30.millis, context.self, Loop)
          Behaviors.same
        case GameEnded =>
          context.log.info("Game has ended, stopping...")
          Behaviors.stopped
      }
  }

}
