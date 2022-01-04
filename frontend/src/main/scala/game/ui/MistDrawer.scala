package game.ui

import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.entities.*
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.GameState
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Application
import typings.pixiJs.mod.{Graphics, Sprite}
import utils.misc.{RGBAColour, RGBColour}

import scala.collection.mutable
import scala.scalajs.js.JSConverters.*

trait MistDrawer extends Drawer {

  def mistContainer: ReactiveContainer
  def camera: Camera

  @inline private def mistStage = mistContainer.ref

  private val mistColour = RGBAColour(255 * 4 / 5, 255 * 4 / 5, 255 * 4 / 5, 0.3)

  private val mistsSprites: mutable.Map[Entity.Id, (typings.pixiJs.PIXI.Graphics, Double)] = mutable.Map()

  def drawMists(mists: Map[Entity.Id, Mist]): Unit = {
    mistsSprites
      .filterNot(elem => mists.isDefinedAt(elem._1))
      .foreach { elem =>
        mistStage.removeChild(elem._2._1)
        mistsSprites -= elem._1
      }

    def addMistSprite(mist: Mist): (typings.pixiJs.PIXI.Graphics, Double) = {
      val localCoordsVertices = mist.shape.vertices
        .map(z => (z.re, -z.im))
        .flatMap(elem => Vector(elem._1, elem._2))
        .toJSArray
      val polygon = new Graphics()
        .beginFill(mistColour.intColour, mistColour.alpha)
        .drawPolygon(localCoordsVertices)
        .endFill()

      mistStage.addChild(polygon)

      (polygon, mist.sideLength)
    }

    mists.foreach { case (id, mist) =>
      val m = mistsSprites.get(id) match {
        case Some(elem) if elem._2 == mist.sideLength =>
          elem
        case Some(elem) =>
          mistStage.removeChild(elem._1)
          val newElem = addMistSprite(mist)
          mistsSprites += (id -> newElem)
          newElem
        case None =>
          val newElem = addMistSprite(mist)
          mistsSprites += (id -> newElem)
          newElem
      }
      camera.viewportManager(m._1, 0, 0, mist.shape.boundingBox)
    }
  }

}
