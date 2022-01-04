package game.ui.pixitexturemakers

import gamelogic.entities.concreteentities.Barrier
import org.scalajs.dom
import org.scalajs.dom.CanvasGradient
import org.scalajs.dom.{html, CanvasRenderingContext2D}
import typings.pixiJs.PIXI.Texture
import utils.domutils.ScalablyTypedScalaJSDomInterop.given
import utils.misc.RGBColour

object BarrierTextureMaker extends TextureMaker {

  def apply(color: RGBColour): Texture = textures.get(color) match {
    case Some(tex) =>
      tex
    case None =>
      val canvas2d: html.Canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]
      val shadingWidth          = 15
      canvas2d.width = Barrier.width.toInt + 2 * shadingWidth
      canvas2d.height = Barrier.length.toInt + 2 * shadingWidth

      val CSSColor = color.rgb

      val ctx: CanvasRenderingContext2D = canvas2d.getContext("2d").asInstanceOf[CanvasRenderingContext2D]

      val gradient1: CanvasGradient = ctx.createLinearGradient(0, 0, shadingWidth, 0)
      gradient1.addColorStop(0, "rgba(100,100,100,0)")
      gradient1.addColorStop(1, CSSColor)

      ctx.fillStyle = gradient1
      ctx.fillRect(0, shadingWidth, shadingWidth, Barrier.length)

      val gradient2: CanvasGradient = ctx.createLinearGradient(canvas2d.width - shadingWidth, 0, canvas2d.width, 0)
      gradient2.addColorStop(0, CSSColor)
      gradient2.addColorStop(1, "rgba(100,100,100,0)")

      ctx.fillStyle = gradient2
      ctx.fillRect(canvas2d.width - shadingWidth, shadingWidth, shadingWidth, Barrier.length)

      val gradient3: CanvasGradient = ctx.createLinearGradient(0, 0, 0, shadingWidth)
      gradient3.addColorStop(1, CSSColor)
      gradient3.addColorStop(0, "rgba(100,100,100,0)")

      ctx.fillStyle = gradient3
      ctx.fillRect(shadingWidth, 0, Barrier.width, shadingWidth)

      val gradient4: CanvasGradient = ctx.createLinearGradient(0, canvas2d.height - shadingWidth, 0, canvas2d.height)
      gradient4.addColorStop(0, CSSColor)
      gradient4.addColorStop(1, "rgba(100,100,100,0)")

      ctx.fillStyle = gradient4
      ctx.fillRect(shadingWidth, canvas2d.height - shadingWidth, Barrier.width, shadingWidth)

      ctx.fillStyle = "rgb(255,255,255)"
      ctx.fillRect(shadingWidth, shadingWidth, Barrier.width, Barrier.length)

      val tex = typings.pixiJs.global.PIXI.Texture.from(canvas2d, (), true: Boolean)

      textures += color -> tex

      tex

  }

}
