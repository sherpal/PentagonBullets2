package gamelogic.gamestate.gameactions

import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.statetransformers.{GameStateTransformer, RemoveEntity}

final case class DestroyBarrier(
    actionId: GameAction.Id,
    time: Long,
    barrierId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer = RemoveEntity(barrierId, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
