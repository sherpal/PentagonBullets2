package services.browser

import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import zio.{Task, UIO, ZIO}

import scala.util.Try

object Browser {

  trait Service {

    def get(url: String): Task[Unit]

    def findElement(by: By): Task[Option[WebElement]]

  }

  object Service {
    def chrome(driver: ChromeDriver): Service = new Service {
      override def get(url: String): Task[Unit] = ZIO.effect(driver.get(url))

      override def findElement(by: By): Task[Option[WebElement]] =
        ZIO.effect(Try(driver.findElement(by.toSeleniumBy)).toOption)
    }
  }

}
