package services.browser
import org.openqa.selenium
import org.openqa.selenium.{By as SeleniumBy, WebElement}
import zio.ZIO

sealed trait By {
  def toSeleniumBy: SeleniumBy

  final def find: ZIO[Browser, Throwable, Option[WebElement]] = findElement(this)

  final def findOrFail: ZIO[Browser, Throwable, WebElement] = findElementOrFail(this)

  final def use[R <: Browser, E >: Throwable, A](effect: WebElement => ZIO[R, E, A]): ZIO[R, E, A] =
    findOrFail.flatMap(effect)
}

object By {

  case class TagName(tag: String) extends By {
    def toSeleniumBy: SeleniumBy = new SeleniumBy.ByTagName(tag)
  }

  case class ClassName(cls: String) extends By {
    override def toSeleniumBy: SeleniumBy = new SeleniumBy.ByClassName(cls)
  }

}
