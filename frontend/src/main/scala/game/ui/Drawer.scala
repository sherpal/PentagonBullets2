package game.ui

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Polygon
import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.HealUnit
import typings.pixiJs.PIXI
import typings.pixiJs.PIXI.DisplayObject
import typings.pixiJs.mod.Sprite
import typings.pixiJs.mod.{Application, Graphics, Point}
import utils.misc.{Colour, RGBColour}

import scala.language.implicitConversions
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.|
import scala.scalajs.js

trait Drawer {

  protected implicit def toPointArray(array: js.Array[Double]): js.Array[Double | typings.pixiJs.PIXI.Point] =
    array.asInstanceOf[js.Array[Double | typings.pixiJs.PIXI.Point]]

  val linearScale = 1.0 //.asInstanceOf[typings.pixiJs.PIXI.SCALE_MODES.LINEAR]

  typings.pixiJs.global.PIXI.RenderTexture

  def application: Application

  protected def diskTexture(
      colour: Int,
      alpha: Double,
      radius: Double,
      withBlackDot: Boolean = false
  ): PIXI.RenderTexture = {
    val graphics = new Graphics
    graphics.lineStyle(0) // draw a circle, set the lineStyle to zero so the circle doesn't have an outline

    graphics.beginFill(colour, alpha)
    graphics.drawCircle(0, 0, radius)
    graphics.endFill()

    if (withBlackDot) {
      graphics.beginFill(0x000000, 1)
      graphics.drawCircle(radius, 0.0, 3.0)
      graphics.endFill()
    }

    application.renderer.generateTexture(graphics, linearScale, 1)
  }

  protected def newDisk(radius: Double, colour: RGBColour, alpha: Double = 1.0): Sprite =
    new Sprite(diskTexture(colour.intColour, alpha, radius))

  protected def circleTexture(colour: Int, alpha: Double, radius: Double): PIXI.RenderTexture = {
    val graphics = new Graphics
    graphics.lineStyle(1, colour, alpha)

    graphics.beginFill(0xffffff, 0.0)
    graphics.drawCircle(0, 0, radius)

    application.renderer.generateTexture(graphics, linearScale, 1)

  }

  protected def newCircle(colour: Colour, radius: Double): Sprite =
    new Sprite(circleTexture(colour.intColour, colour.alpha, radius))

  protected def polygonTexture(
      colour: Int,
      alpha: Double,
      shape: Polygon
  ): PIXI.RenderTexture = {
    val graphics = new Graphics
    graphics
      .lineStyle(0)
      .beginFill(colour, alpha)
      .drawPolygon(
        shape.vertices
          .map(_.conjugate)
          .map { case Complex(re, im) =>
            new Point(re, im)
          }
          .toJSArray
          .asInstanceOf[scala.scalajs.js.Array[Double | typings.pixiJs.PIXI.Point]]
      )

    application.renderer.generateTexture(graphics, linearScale, 1)
  }

  protected def newHealUnit(): Sprite = {
    val healCrossVertices = Vector(
      Complex(3, -1),
      Complex(3, 1),
      Complex(1, 1),
      Complex(1, 3),
      Complex(-1, 3),
      Complex(-1, 1),
      Complex(-3, 1),
      Complex(-3, -1),
      Complex(-1, -1),
      Complex(-1, -3),
      Complex(1, -3),
      Complex(1, -1)
    ).map(_ * HealUnit.radius / 5)
      .map(~_)
      .map(_ + Complex(2 * HealUnit.radius.toInt, 2 * HealUnit.radius.toInt))
      .flatMap(z => List(z.re, z.im))
    new Sprite(
      application.renderer.generateTexture(
        new Graphics()
          .beginFill(RGBColour.white.intColour)
          .drawCircle(2 * HealUnit.radius.toInt, 2 * HealUnit.radius.toInt, HealUnit.radius.toInt)
          .endFill()
          .beginFill(RGBColour.green.intColour)
          .drawPolygon(healCrossVertices.toJSArray)
          .endFill(),
        linearScale,
        1
      )
    )
  }

  /** When defined, returns the [[DisplayObject]] representing, in the game, the entity with the given id.
    */
  def maybeEntityDisplayObjectById(entityId: Entity.Id): Option[DisplayObject]

}
