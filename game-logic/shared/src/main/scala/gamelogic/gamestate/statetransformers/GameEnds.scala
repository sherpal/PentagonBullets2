package gamelogic.gamestate.statetransformers
import gamelogic.gamestate.GameState

final class GameEnds(time: Long) extends GameStateTransformer {
  override def apply(gameState: GameState): GameState = gameState.ends(time)
}
