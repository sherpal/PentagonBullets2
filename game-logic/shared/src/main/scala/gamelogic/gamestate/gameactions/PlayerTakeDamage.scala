package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Happens when a Player takes damage from a specified source, other than a Bullet.
  */
final case class PlayerTakeDamage(
    actionId: GameAction.Id,
    time: Long,
    playerId: Entity.Id,
    sourceId: Entity.Id,
    damage: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[Player](playerId, time, player => player.copy(lifeTotal = player.lifeTotal - damage))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
