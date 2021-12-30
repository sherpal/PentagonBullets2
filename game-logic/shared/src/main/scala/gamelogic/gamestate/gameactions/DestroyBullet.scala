package gamelogic.gamestate.gameactions

import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.entities.{ActionSource, Entity}

/** This action happens when a Bullet is destroyed because it ran on more than Bullet.reach pixels.
  */
final case class DestroyBullet(
    actionId: GameAction.Id,
    bulletId: Entity.Id,
    time: Long,
    actionSource: ActionSource
) extends GameAction
    with DestroyEntity {

  def entityId: Entity.Id = bulletId

  def applyDefault(gameState: GameState): GameState = gameState.removeEntityById(bulletId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
