package game.ui

import be.doeraene.physics.Complex
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

trait HealUnitDrawer extends Drawer {
  def healUnitContainer: ReactiveContainer
  def camera: Camera

  @inline private def healUnitStage = healUnitContainer.ref

  private val healUnits: mutable.Map[Entity.Id, Sprite] = mutable.Map()

  def drawHealUnits(hus: Map[Entity.Id, HealUnit]): Unit = {
    healUnits
      .filterNot(elem => hus.isDefinedAt(elem._1))
      .foreach { elem =>
        healUnitStage.removeChild(elem._2)
        healUnits -= elem._1
      }

    hus.foreach { case (id, healUnit) =>
      camera.viewportManager(
        healUnits.getOrElse(
          id, {
            val elem = newHealUnit()
            healUnitStage.addChild(elem)

            healUnits += (id -> elem)
            elem
          }
        ),
        healUnit.pos + Complex(-HealUnit.radius, HealUnit.radius),
        healUnit.pos,
        healUnit.shape.boundingBox
      )
    }
  }

}
