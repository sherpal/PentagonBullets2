package game.ui

import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import be.doeraene.physics.Complex
import game.Camera
import be.doeraene.physics.shape.BoundingBox
import utils.misc.RGBColour
import typings.pixiJs.mod.{DisplayObject as _, *}
import gamelogic.gamestate.GameState
import gamelogic.entities.*
import gamelogic.entities.concreteentities.Barrier
import game.ui.pixitexturemakers.BarrierTextureMaker

import scala.collection.mutable

trait BarrierDrawer extends Drawer {

  def barrierContainer: ReactiveContainer
  def camera: Camera

  @inline private def barrierStage = barrierContainer.ref

  val barrierSprites: mutable.Map[Entity.Id, Sprite] = mutable.Map()

  def drawBarriers(state: GameState, colors: Map[Entity.Id, RGBColour]): Unit = {
    barrierSprites.toMap
      .filterNot(e => state.allTEntities[Barrier].isDefinedAt(e._1))
      .foreach { case (id, sprite) =>
        barrierSprites -= id
        barrierStage.removeChild(sprite)
      }

    state.allTEntities[Barrier].foreach { case (id, barrier) =>
      val sprite = barrierSprites.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val sprite = new Sprite(BarrierTextureMaker(colors(barrier.ownerId)))
          sprite.anchor.set(0.5, 0.5)

          barrierSprites += id -> sprite
          barrierStage.addChild(sprite)

          sprite.rotation = -barrier.rotation

          sprite
      }

      camera.viewportManager(sprite, barrier.pos, barrier.shape.boundingBox)
    }
  }

}
