package game.ui.gui.reactivecomponents

import com.raquo.laminar.api.A.*
import game.ui.reactivepixi.AttributeModifierBuilder._
import game.ui.reactivepixi.ReactivePixiElement._
import typings.pixiJs.PIXI.{Graphics, Texture}
import utils.misc.Colour

/** Helper class for displaying bar on screen.
  *
  * As extending [[game.ui.gui.components.GUIComponent]], you have access to the `container` member to set dimensions
  * and alpha.
  *
  * @param barFillingSignal
  *   the percentage of filling that this bar must have (number between 0 and 1).
  * @param colourSignal
  *   computes the tint value the bar must have
  * @param visibleSignal
  *   determine whether this bar should be visible
  * @param texture
  *   texture to draw the bar.
  */
final class StatusBar(
    barFillingSignal: Signal[Double],
    colourSignal: Signal[Colour],
    visibleSignal: Signal[Boolean],
    texture: Texture,
    dimensions: Signal[(Double, Double)], // signal of width and height
    orientation: StatusBar.BarOrientation = StatusBar.Horizontal
) extends GUIComponent {

  private def graphicsFilling(filling: Double, width: Double, height: Double): Graphics => Unit =
    if orientation == StatusBar.Horizontal then
      (_: Graphics).clear().beginFill(0xc0c0c0).drawRect(0, 0, width * filling, height)
    else (_: Graphics).clear().beginFill(0xc0c0c0).drawRect(0, height * (1 - filling), width, height)

  val graphicsMask: ReactiveGraphics = pixiGraphics(
    moveGraphics <-- barFillingSignal.combineWith(dimensions).map(graphicsFilling)
  )

  container.amend(
    pixiSprite(
      texture,
      width   <-- dimensions.map(_._1),
      height  <-- dimensions.map(_._2),
      visible <-- visibleSignal,
      tint    <-- colourSignal,
      alpha   <-- colourSignal.map(_.alpha),
      mask := graphicsMask
    ),
    graphicsMask
  )

}

object StatusBar {

  sealed trait BarOrientation
  case object Horizontal extends BarOrientation
  case object Vertical extends BarOrientation

}
