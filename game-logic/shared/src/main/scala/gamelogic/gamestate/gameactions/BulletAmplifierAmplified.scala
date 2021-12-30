package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.BulletAmplifier
import gamelogic.gamestate.statetransformers.{GameStateTransformer, TransformEntity}
import gamelogic.gamestate.{GameAction, GameState}

/** Adds a Bullet id to the list of amplified bullets.
  */
final case class BulletAmplifierAmplified(
    actionId: GameAction.Id,
    time: Long,
    bulletId: Entity.Id,
    amplifierId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[BulletAmplifier](amplifierId, time, _.addBulletAmplified(bulletId))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
