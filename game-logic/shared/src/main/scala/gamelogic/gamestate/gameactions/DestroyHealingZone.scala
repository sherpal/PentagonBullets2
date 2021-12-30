package gamelogic.gamestate.gameactions

import gamelogic.entities.Entity.Id
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.{GameAction, GameState}

/** Happens when a zone lost all its supply.
  */
final case class DestroyHealingZone(
    actionId: GameAction.Id,
    time: Long,
    zoneId: Entity.Id,
    actionSource: ActionSource
) extends GameAction
    with DestroyEntity {

  def entityId: Id = zoneId

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
