package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.DamageZone
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Updates (or creates) the DamageZone at the specified position and with the specified radius.
  */
final case class UpdateDamageZone(
    actionId: GameAction.Id,
    time: Long,
    id: Entity.Id,
    lastGrow: Long,
    lastTick: Long,
    xPos: Double,
    yPos: Double,
    radius: Int,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(DamageZone(id, lastGrow, lastTick, xPos, yPos, radius), time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
