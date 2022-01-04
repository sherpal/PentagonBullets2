package game.ui.pixitexturemakers

import gamelogic.entities.concreteentities.GunTurret
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.{Application, Graphics, Point}
import utils.domutils.ScalablyTypedScalaJSDomInterop.given
import utils.misc.RGBColour

final class GunTurretTextureMaker(application: Application) extends TextureMaker {

  def apply(color: RGBColour): Texture = apply(color, GunTurret.defaultRadius)

  def apply(color: RGBColour, radius: Double): Texture = textures.get(color) match {
    case Some(tex) =>
      tex
    case None =>
      // 2 times radius to be sure that the graphics will not cut the image
      val (x, y) = (2 * radius, 2 * radius)
      val graphics = new Graphics()
        .beginFill(color.intColour)
        .drawCircle(x, y, radius)
        .endFill()
        .beginFill(0xffffff, 0.8)
        .drawCircle(x, y, radius / 3)
        .endFill()
        .beginFill(0xffffff)
        .drawRect(x, y - 2, radius * 5 / 4, 4)
        .endFill()
      val tex = application.renderer.generateTexture(graphics, 1.0, 1)
      textures += color -> tex
      tex
  }

}
