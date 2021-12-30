package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.statetransformers.GameStateTransformer
import gamelogic.gamestate.{GameAction, GameState}

/** PlayerHitByBullet when a Player is hit by an opponent's bullet.
  */
final case class PlayerHitByBullet(
    actionId: GameAction.Id,
    playerId: Entity.Id,
    bulletId: Entity.Id,
    damage: Double,
    time: Long,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    PlayerHitByMultipleBullets(actionId, time, List(bulletId), playerId, damage, actionSource)
      .createGameStateTransformer(gameState)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
