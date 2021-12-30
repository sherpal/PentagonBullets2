package gamelogic.gamestate.statetransformers
import gamelogic.gamestate.GameState
import gamelogic.entities.Entity

final class RemoveDeadPlayer(playerId: Entity.Id, time: Long) extends GameStateTransformer {
  override def apply(gameState: GameState): GameState = gameState.removeDeadPlayer(playerId, time)
}
