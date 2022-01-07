package gamelogic.gamestate.gameactions

import gamelogic.buffs.Buff
import gamelogic.entities.Entity
import gamelogic.gamestate.GameAction.Id
import gamelogic.gamestate.statetransformers.{GameStateTransformer, RemoveBuffTransformer}
import gamelogic.gamestate.{GameAction, GameState}

/** Removes the given buff from the given entity. */
final case class RemoveBuff(actionId: GameAction.Id, time: Long, bearerId: Entity.Id, buffId: Buff.Id)
    extends GameAction {
  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    new RemoveBuffTransformer(time, bearerId, buffId)

  def setId(newId: Id): RemoveBuff = copy(actionId = newId)

  def changeTime(newTime: Long): RemoveBuff = copy(time = newTime)
}
