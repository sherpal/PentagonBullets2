package gamelogic.gamestate.gameactions

import gamelogic.entities.ActionSource
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** GameAction that happen when a new Player comes in onto the game, or if a Player property changes.
  */
final case class NewPlayer(
    actionId: GameAction.Id,
    player: Player,
    time: Long,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(player, time) ++ RemoveDeadPlayer(player.id, time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
