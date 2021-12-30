package gamelogic.gamestate.statetransformers
import gamelogic.gamestate.GameState
import gamelogic.entities.concreteentities.Player

final class WithDeadPlayer(time: Long, player: Player) extends GameStateTransformer {
  def apply(gameState: GameState): GameState = gameState.withDeadPlayer(time, player)
}
