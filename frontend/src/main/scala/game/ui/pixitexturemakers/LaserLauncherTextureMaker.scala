package game.ui.pixitexturemakers

import gamelogic.entities.concreteentities.LaserLauncher
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.{Application, Graphics, Point}
import utils.domutils.ScalablyTypedScalaJSDomInterop.given
import utils.misc.RGBColour

final class LaserLauncherTextureMaker(application: Application) extends TextureMaker {

  def apply(color: RGBColour): Texture = textures.get(color) match {
    case Some(tex) =>
      tex
    case None =>
      val radius   = LaserLauncher.laserLauncherShapeRadius
      val (x, y)   = (2 * radius, 2 * radius)
      val intColor = color.intColour
      val graphics = new Graphics()
        .beginFill(0x999999, 0.5)
        .drawRoundedRect(radius, radius, 2 * radius, 2 * radius, radius / 5)
        .endFill()
        .beginFill(intColor)
        .drawCircle(x, y, radius / 3)
        .endFill()
        .lineStyle(radius / 5, intColor)
        .drawRoundedRect(radius, radius, 2 * radius, 2 * radius, radius / 5)
      val tex = application.renderer.generateTexture(graphics, 1.0, 1)
      textures += color -> tex
      tex
  }

}
