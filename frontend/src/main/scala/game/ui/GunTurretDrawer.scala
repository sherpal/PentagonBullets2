package game.ui

import game.Camera
import game.ui.pixitexturemakers.GunTurretTextureMaker
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.entities.*
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.GameState
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Application
import typings.pixiJs.mod.Sprite
import utils.misc.RGBColour

import scala.collection.mutable

trait GunTurretDrawer {
  def gunTurretContainer: ReactiveContainer
  def camera: Camera

  def application: Application

  lazy val gunTurretTextureMaker = new GunTurretTextureMaker(application)

  @inline private def gunTurretStage = gunTurretContainer.ref

  private val gunTurretSprites: mutable.Map[Entity.Id, Sprite] = mutable.Map()

  def drawGunTurrets(state: GameState, colors: Map[Entity.Id, RGBColour]): Unit = {
    gunTurretSprites
      .filterNot(elem => state.entityIdExistsAs[GunTurret](elem._1))
      .foreach { elem =>
        gunTurretStage.removeChild(elem._2)
        gunTurretSprites -= elem._1
      }

    state.allTEntities[GunTurret].foreach { case (turretId, turret) =>
      val elem = gunTurretSprites.get(turretId) match {
        case Some(e) =>
          e
        case None =>
          val turretSprite = new Sprite(gunTurretTextureMaker(colors(turret.ownerId), turret.radius))
          turretSprite.anchor.set(0.5, 0.5)
          gunTurretSprites += (turretId -> turretSprite)
          gunTurretStage.addChild(turretSprite)
          turretSprite
      }
      elem.rotation = -turret.rotation
      camera.viewportManager(elem, turret.pos, turret.shape.boundingBox)
    }
  }

}
