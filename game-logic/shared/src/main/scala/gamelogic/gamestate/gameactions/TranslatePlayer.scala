package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class TranslatePlayer(
    actionId: GameAction.Id,
    time: Long,
    playerId: Entity.Id,
    newPos: Complex,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[Player](playerId, time, _.copy(time = time, pos = newPos))

  override def canHappen(gameState: GameState): Boolean = gameState.isPlayerAlive(playerId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
