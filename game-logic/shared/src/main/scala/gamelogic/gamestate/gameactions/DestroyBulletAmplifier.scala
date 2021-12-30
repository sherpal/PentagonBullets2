package gamelogic.gamestate.gameactions

import gamelogic.entities.Entity.Id
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.{GameAction, GameState}

/** Removes a [[entities.BulletAmplifier]] from the game.
  */
final case class DestroyBulletAmplifier(
    actionId: GameAction.Id,
    time: Long,
    bulletAmplifierId: Entity.Id,
    actionSource: ActionSource
) extends GameAction
    with DestroyEntity {

  def entityId: Id = bulletAmplifierId

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
