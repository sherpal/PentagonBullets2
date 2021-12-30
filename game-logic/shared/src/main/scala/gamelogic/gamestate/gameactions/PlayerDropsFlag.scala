package gamelogic.gamestate.gameactions

import gamelogic.entities.{concreteentities, ActionSource, Entity}
import gamelogic.entities.concreteentities.TeamFlag
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Happens when the flag bearer is killed. The Flag goes back to its original position.
  */
final case class PlayerDropsFlag(
    actionId: GameAction.Id,
    time: Long,
    flagId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    new TransformEntity[TeamFlag](flagId, time, _.withoutBearer)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
