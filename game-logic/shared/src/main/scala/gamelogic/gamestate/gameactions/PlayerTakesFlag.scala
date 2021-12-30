package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.TeamFlag
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class PlayerTakesFlag(
    actionId: GameAction.Id,
    time: Long,
    flagId: Entity.Id,
    playerId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  override def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[TeamFlag](flagId, time, _.withBearer(playerId))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
