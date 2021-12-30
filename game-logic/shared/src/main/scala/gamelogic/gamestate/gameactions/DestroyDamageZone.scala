package gamelogic.gamestate.gameactions

import gamelogic.entities.Entity.Id
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.{GameAction, GameState}

final case class DestroyDamageZone(
    actionId: GameAction.Id,
    time: Long,
    id: Entity.Id,
    actionSource: ActionSource
) extends GameAction
    with DestroyEntity {

  def entityId: Id = id

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
