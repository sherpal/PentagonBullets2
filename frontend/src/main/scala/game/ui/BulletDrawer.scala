package game.ui

import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.entities.*
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.GameState
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Application
import typings.pixiJs.mod.Sprite
import utils.misc.RGBColour

import scala.collection.mutable

trait BulletDrawer extends Drawer {

  def bulletContainer: ReactiveContainer
  def camera: Camera

  @inline private def bulletStage = bulletContainer.ref

  private val bullets: mutable.Map[Entity.Id, Sprite] = mutable.Map()

  def drawBullets(state: GameState, time: Long, colors: Map[Entity.Id, RGBColour]): Unit = {
    val bs = state.allTEntities[BulletLike]
    bullets
      .filterNot(elem => bs.isDefinedAt(elem._1))
      .foreach { elem =>
        bulletStage.removeChild(elem._2)
        bullets -= elem._1
      }

    bs.foreach { case (id, bullet) =>
      val newElem = bullets.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val elem = newDisk(bullet.radius, colors(bullet.ownerId))
          bulletStage.addChild(elem)

          elem.anchor.set(0.5, 0.5)

          bullets += (id -> elem)
          elem
      }
      camera.viewportManager(newElem, bullet.currentPosition(time), bullet.shape.boundingBox)
    }
  }

}
