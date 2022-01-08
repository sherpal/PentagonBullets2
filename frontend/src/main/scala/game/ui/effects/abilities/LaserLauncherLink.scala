package game.ui.effects.abilities

import game.Camera
import game.ui.Drawer
import game.ui.effects.GameEffect
import gamelogic.entities.Entity
import gamelogic.gamestate.GameState
import typings.pixiJs.mod.Container
import gamelogic.entities.concreteentities.LaserLauncher
import utils.misc.RGBColour
import typings.pixiJs.mod.{Application, Graphics, Point}

//noinspection TypeAnnotation
final class LaserLauncherLink(playerId: Entity.Id, camera: Camera) extends GameEffect {

  val intColour = RGBColour.red.intColour

  val graphics = new Graphics()
    .beginFill(intColour)
    .drawCircle(100, 100, 50)
    .endFill()

  def destroy(): Unit = graphics.destroy()

  def update(currentTime: Long, gameState: GameState): Unit =
    (gameState.playerById(playerId), gameState.laserLaunchers.values.find(_.ownerId == playerId)) match {
      case (Some(player), Some(launcher)) =>
        graphics.visible = true
        val playerPos   = player.currentPosition(currentTime)
        val launcherPos = launcher.pos

        val localPlayerPos   = camera.worldToLocal(playerPos)
        val localLauncherPos = camera.worldToLocal(launcherPos)

        graphics
          .clear()
          .beginFill(intColour)
          .lineStyle(2, intColour)
          .moveTo(localPlayerPos.re, localPlayerPos.im)
          .lineTo(localLauncherPos.re, localLauncherPos.im)
          .endFill()
      case _ =>
        if (graphics.visible)
          graphics.visible = false
    }

  def isOver(currentTime: Long, gameState: GameState): Boolean =
    !gameState.laserLaunchers.values.exists(_.ownerId == playerId)

  def addToContainer(container: Container): Unit = container.addChild(graphics)

}
