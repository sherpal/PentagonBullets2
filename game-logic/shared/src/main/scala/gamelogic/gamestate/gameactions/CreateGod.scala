package gamelogic.gamestate.gameactions

import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.God
import gamelogic.gamestate.statetransformers.*

final case class CreateGod(actionId: GameAction.Id, time: Long, godId: Entity.Id) extends GameAction {
  def setId(newId: GameAction.Id): CreateGod = copy(actionId = newId)

  def changeTime(newTime: Long): CreateGod = copy(time = newTime)

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    new WithEntity(new God(godId, time), time)

  override def canHappen(gameState: GameState): Boolean = gameState.allTEntities[God].isEmpty
}
