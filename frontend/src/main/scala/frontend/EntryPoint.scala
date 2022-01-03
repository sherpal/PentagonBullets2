package frontend

import com.raquo.laminar.api.L.*
import frontend.AppState.NameRequired
import zio.*
import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

object EntryPoint {
  def main(args: Array[String]): Unit = {

    val containerDiv = UIO(Option(dom.document.getElementById("root"))).someOrFail(
      new RuntimeException("Failed to find laminar container.")
    )

    def renderApp(container: dom.Element) = UIO(render(container, App()))

    for {
      _ <- zio.Runtime.default.unsafeRunToFuture(initialiseRuntime)
      _ <- runtime.unsafeRunToFuture(containerDiv >>= renderApp)
    } yield ()
  }
}
