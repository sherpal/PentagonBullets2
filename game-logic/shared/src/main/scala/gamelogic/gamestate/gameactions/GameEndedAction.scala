package gamelogic.gamestate.gameactions

import gamelogic.entities.ActionSource
import gamelogic.gamestate.statetransformers.{GameEnds, GameStateTransformer}
import gamelogic.gamestate.{GameAction, GameState}

final case class GameEndedAction(
    actionId: GameAction.Id,
    time: Long,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer = GameEnds(time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
