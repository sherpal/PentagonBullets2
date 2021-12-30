package gamelogic.gamestate.statetransformers

import gamelogic.entities.Entity
import gamelogic.gamestate.GameState

final class WithEntity(entity: Entity, time: Long) extends GameStateTransformer {
  def apply(gameState: GameState): GameState = gameState.withEntity(entity.id, time, entity)
}
