package game.ui.pixitexturemakers

import scala.collection.mutable
import typings.pixiJs.PIXI.Texture
import utils.misc.RGBColour

trait TextureMaker {

  protected val textures: mutable.Map[RGBColour, Texture] = mutable.Map()

  def apply(color: RGBColour): Texture

}
