package gamelogic.gamestate.statetransformers
import gamelogic.gamestate.GameState
import be.doeraene.physics.shape.Polygon

final class GameBegins(time: Long, bounds: Polygon) extends GameStateTransformer {
  override def apply(gameState: GameState): GameState = gameState.starts(time, bounds)
}
