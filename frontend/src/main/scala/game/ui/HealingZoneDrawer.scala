package game.ui

import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Sprite
import utils.misc.RGBColour
import gamelogic.gamestate.GameState
import gamelogic.entities.concreteentities.HealingZone
import gamelogic.entities.*

import scala.collection.mutable

trait HealingZoneDrawer extends Drawer {

  def healingZoneTexture: Texture

  def healingZoneContainer: ReactiveContainer
  def camera: Camera

  @inline private def healingZoneStage = healingZoneContainer.ref

  def newHealingZone(color: RGBColour): Sprite = {
    val sprite = new Sprite(healingZoneTexture)
    sprite.tint = color.intColour
    sprite
  }

  private val healingZoneSprites: mutable.Map[Entity.Id, Sprite] = mutable.Map()

  def drawHealingZones(state: GameState, colors: Map[Entity.Id, RGBColour]): Unit = {
    val healingZones = state.allTEntities[HealingZone]

    healingZoneSprites.toMap.foreach { case (id, sprite) =>
      if (!healingZones.isDefinedAt(id)) {
        healingZoneSprites -= id
        healingZoneStage.removeChild(sprite)
      }
    }

    healingZones.foreach { case (id, zone) =>
      val healingZoneSprite = healingZoneSprites.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val elem = newHealingZone(colors(zone.ownerId))
          healingZoneStage.addChild(elem)
          elem.anchor.set(0.5, 0.5)
          healingZoneSprites += (id -> elem)
          elem
      }
      camera.viewportManager(healingZoneSprite, zone.pos, zone.shape.boundingBox)
    }

  }

}
