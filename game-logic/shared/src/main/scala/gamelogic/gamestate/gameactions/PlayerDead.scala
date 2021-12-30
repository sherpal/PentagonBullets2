package gamelogic.gamestate.gameactions

import gamelogic.entities.concreteentities.Player
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** PlayerDead happens when a Player dies.
  */
final case class PlayerDead(
    actionId: GameAction.Id,
    time: Long,
    playerId: Entity.Id,
    playerName: String,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    RemoveEntity(playerId, time) ++ GameStateTransformer.maybe(
      gameState.playerById(playerId).map(WithDeadPlayer(time, _))
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
