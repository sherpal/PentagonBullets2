package game.ui

import game.Camera
import game.ui.pixitexturemakers.LaserLauncherTextureMaker
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.entities.*
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.GameState
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Application
import typings.pixiJs.mod.Sprite
import utils.misc.RGBColour

import scala.collection.mutable

trait LaserLauncherDrawer extends Drawer {
  def laserLauncherContainer: ReactiveContainer
  def camera: Camera

  def application: Application

  lazy val laserLauncherTextureMaker = new LaserLauncherTextureMaker(application)

  @inline private def laserLauncherStage = laserLauncherContainer.ref

  private val laserLauncherSprites: mutable.Map[Entity.Id, Sprite] = mutable.Map()

  def drawLaserLauncher(state: GameState, colors: Map[Entity.Id, RGBColour]): Unit = {

    val laserLaunchers = state.allTEntities[LaserLauncher]

    laserLauncherSprites
      .filterNot(elem => laserLaunchers.isDefinedAt(elem._1))
      .foreach { elem =>
        laserLauncherStage.removeChild(elem._2)
        laserLauncherSprites -= elem._1
      }

    laserLaunchers.foreach { case (laserLauncherId, laserLauncher) =>
      val elem = laserLauncherSprites.getOrElse(
        laserLauncherId, {
          val laserLauncherSprite = new Sprite(laserLauncherTextureMaker(colors(laserLauncher.ownerId)))
          laserLauncherSprite.anchor.set(0.5, 0.5)
          laserLauncherSprites += (laserLauncherId -> laserLauncherSprite)
          laserLauncherStage.addChild(laserLauncherSprite)
          laserLauncherSprite
        }
      )
      camera.viewportManager(elem, laserLauncher.pos, laserLauncher.shape.boundingBox)
    }

  }

}
