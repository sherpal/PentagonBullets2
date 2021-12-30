package gamelogic.gamestate.statetransformers

import gamelogic.entities.Entity
import gamelogic.gamestate.GameState

final class RemoveEntity(entityId: Entity.Id, time: Long) extends GameStateTransformer {
  def apply(gameState: GameState): GameState = gameState.removeEntityById(entityId, time)
}
