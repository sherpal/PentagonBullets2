package game.ui

import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.entities.*
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.GameState
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Application
import typings.pixiJs.mod.Sprite
import utils.misc.{RGBAColour, RGBColour}

import scala.collection.mutable

trait DamageZoneDrawer extends Drawer {

  def damageZoneContainer: ReactiveContainer
  def camera: Camera

  @inline private def damageZoneStage = damageZoneContainer.ref

  private val damageZones: mutable.Map[Entity.Id, Sprite] = mutable.Map()

  private val damageZoneColour = RGBAColour(255, 0, 255, 0.3)

  def drawDamageZones(zones: Map[Entity.Id, DamageZone]): Unit = {
    damageZones
      .filterNot(elem => zones.isDefinedAt(elem._1))
      .foreach { elem =>
        damageZoneStage.removeChild(elem._2)
        damageZones -= elem._1
      }

    zones.foreach { case (id, zone) =>
      val z = damageZones.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val elem = newDisk(DamageZone.maxRadius, damageZoneColour.withoutAlpha)
          damageZoneStage.addChild(elem)
          elem.alpha = damageZoneColour.alpha
          elem.anchor.set(0.5, 0.5)

          damageZones += (id -> elem)
          elem
      }
      camera.viewportManager(z, zone.pos, zone.pos, zone.shape.boundingBox)
      z.width = 2 * zone.radius * camera.scaleX
      z.height = 2 * zone.radius * camera.scaleY
    }
  }

}
