package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.statetransformers.{GameStateTransformer, RemoveEntity}
import gamelogic.gamestate.{GameAction, GameState}

final case class DestroySmashBullet(
    actionId: GameAction.Id,
    time: Long,
    bulletId: Entity.Id,
    actionSource: ActionSource
) extends GameAction
    with DestroyEntity {

  def entityId: Entity.Id = bulletId

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
