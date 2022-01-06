package game.ui

import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.buffs.Buff
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Sprite
import utils.misc.RGBColour
import gamelogic.gamestate.GameState
import gamelogic.entities.concreteentities.*
import gamelogic.entities.*

import scala.collection.mutable

trait BuffsDrawer extends Drawer {

  def playerContainer: ReactiveContainer
  def camera: Camera

  @inline private def playerStage = playerContainer.ref

  private val playerBuffs: mutable.Map[Buff.Id, Sprite] = mutable.Map()

  def drawPlayerBuffs(state: GameState, time: Long): Unit = {

    val shields = state.passiveTBuffsById[Shield]

    playerBuffs
      .filterNot(elem => shields.isDefinedAt(elem._1))
      .foreach { (id, sprite) =>
        playerStage.removeChild(sprite)
        playerBuffs -= id
      }

    shields.foreach { case (id, shield) =>
      val buff = playerBuffs.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val elem = newCircle(RGBColour.white, Player.radius.toInt + 2)
          playerStage.addChild(elem)

          elem.anchor.set(0.5, 0.5)

          playerBuffs += (id -> elem)

          elem
      }

      state.players.get(shield.bearerId) match {
        case Some(player) =>
          val playerPos = player.currentPosition(time, state.obstacles.values)
          camera.viewportManager(buff, playerPos, player.shape.boundingBox)
        case None =>
          buff.visible = false
      }
    }

  }

}
