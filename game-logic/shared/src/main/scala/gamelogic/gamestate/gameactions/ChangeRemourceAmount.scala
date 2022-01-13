package gamelogic.gamestate.gameactions

import gamelogic.entities.Resource.ResourceAmount
import gamelogic.entities.{Entity, WithAbilities}
import gamelogic.gamestate.GameAction.Id
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class ChangeRemourceAmount(actionId: GameAction.Id, time: Long, entityId: Entity.Id, resourceDelta: ResourceAmount) extends GameAction {

  def setId(newId: Id): GameAction = copy(actionId = newId)

  def changeTime(newTime: Long): ChangeRemourceAmount = copy(time = newTime)

  override def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    new TransformEntity[WithAbilities](entityId, time, _.resourceAmountChange(resourceDelta))

}
