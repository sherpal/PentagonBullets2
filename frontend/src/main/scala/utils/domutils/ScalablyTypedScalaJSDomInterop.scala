package utils.domutils

import org.scalajs.dom
import typings.std.HTMLCanvasElement

object ScalablyTypedScalaJSDomInterop {

  given Conversion[typings.std.HTMLCanvasElement, dom.html.Canvas] with
    override def apply(x: HTMLCanvasElement): dom.html.Canvas = x.asInstanceOf[dom.html.Canvas]
}
