package gamelogic.gamestate.gameactions

import gamelogic.entities.Entity
import gamelogic.gamestate.statetransformers.{GameStateTransformer, RemoveEntity}
import gamelogic.gamestate.{GameAction, GameState}

trait DestroyEntity { self: GameAction =>
  def entityId: Entity.Id

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    RemoveEntity(entityId, time)
}
