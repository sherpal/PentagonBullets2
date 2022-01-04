package utils.domutils

import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import typings.std.HTMLCanvasElement

object ScalablyTypedScalaJSDomInterop {

  given Conversion[typings.std.HTMLCanvasElement, dom.html.Canvas] with
    override def apply(x: HTMLCanvasElement): dom.html.Canvas = x.asInstanceOf[dom.html.Canvas]

  given Conversion[dom.html.Canvas, typings.std.HTMLCanvasElement] with
    override def apply(x: dom.html.Canvas): HTMLCanvasElement = x.asInstanceOf[HTMLCanvasElement]
}
