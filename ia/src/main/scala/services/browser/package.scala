package services

import org.openqa.selenium.WebElement
import zio._

package object browser {

  type Browser = Has[Browser.Service]

  private val serviceWith = ZIO.serviceWith[Browser.Service]

  def get(url: String): ZIO[Browser, Throwable, Unit] = serviceWith(_.get(url))

  def findElement(by: By): ZIO[Browser, Throwable, Option[WebElement]] = serviceWith(_.findElement(by))

  def findElementOrFail(by: By) =
    findElement(by).someOrFail(new RuntimeException(s"Could not find element satisfying $by."))
}
