package ia

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.By.ByTagName
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.Keys
import services.browser.Browser
import zio.*
import services.browser.*
import zio.duration.*

import java.util.Base64
import gamecommunication.given_Pickler_GameState
import boopickle.Default.*
import gamelogic.gamestate.GameState
import org.openqa.selenium.interactions.Actions

import scala.util.Try

object Main {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(getClass)

    logger.info("IA for pb!")

    //WebDriverManager.chromedriver().setup()

    System.setProperty("webdriver.chrome.driver", "./chromedriver.exe")

    val options = new ChromeOptions()
    val driver  = new ChromeDriver(options)

    val system  = ActorSystem(Behaviors.ignore, "PBAI")
    val aiActor = system.systemActorOf(AIActor(driver), "AIActor")

    val browserLayer = zio.ZLayer.succeed(Browser.Service.chrome(driver))

    val gameStateSer = By.ClassName("gameStateSer").find

    val gameStateSerWhenGameStarts = gameStateSer
      .zipLeft(zio.clock.sleep(1.second))
      .repeatUntil {
        case None            => false
        case Some(container) => Try(container.findElement(new ByTagName("div"))).toOption.isDefined
      }
      .map(_.get)

    val program =
      for {
        _       <- get("http://localhost:9000")
        element <- findElementOrFail(By.TagName("input"))
        _ <- ZIO.effect {
          element.sendKeys("TheCoolAI")
          element.sendKeys(Keys.ENTER)
        }
        _ <- By
          .ClassName("ready-checkbox")
          .use(readyCheckBox =>
            ZIO.effect {
              readyCheckBox.click()
            }
          )
        _ <- gameStateSerWhenGameStarts

        gameStateStream <- zio.stream.ZStream
          .fromSchedule(Schedule.spaced(10.millis))
          .mapM(_ =>
            gameStateSer
              .repeatUntil(_.isDefined)
              .map(_.get)
              .flatMap { element =>
                ZIO
                  .effect {
                    val gameStateInfoContainer = element
                      .findElement(new ByTagName("div"))
                    Try(gameStateInfoContainer.getText).toOption
                  }
              }
          )
          .collect { case Some(base64String) => base64String }
          .map(str => Base64.getDecoder.decode(str))
          .map(array => java.nio.ByteBuffer.wrap(array))
          .map(Unpickle[GameState].fromBytes)
          .takeUntil(_.ended)
          .foreach(gameState => ZIO.effectTotal(aiActor ! AIActor.RefreshGameState(gameState))) race zio.clock
          .sleep(20.seconds)
          .andThen(ZIO.effectTotal(println("Stopping")))
        _ <- ZIO.effectTotal(aiActor ! AIActor.GameEnded)
      } yield ()

    val runtime = Runtime.default.unsafeRun(
      ZIO
        .runtime[Browser with zio.clock.Clock]
        .provideLayer(
          browserLayer ++ zio.clock.Clock.live
        )
    )

    runtime.unsafeRun(
      program
        .ensuring(ZIO.effectTotal(println("Clean up")) *> ZIO.effectTotal(driver.quit()))
        .ensuring(ZIO.effectTotal(system.terminate()))
    )

  }
}
