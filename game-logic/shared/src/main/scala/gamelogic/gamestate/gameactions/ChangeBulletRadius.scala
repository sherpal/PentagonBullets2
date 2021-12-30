package gamelogic.gamestate.gameactions

import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.*
import gamelogic.entities.ActionSource
import gamelogic.gamestate.statetransformers.{GameStateTransformer, TransformEntity}
import gamelogic.gamestate.{GameAction, GameState}

/** Changes the radius of a given bullet.
  *
  * This may happen if a bullet hits a [[entities.BulletAmplifier]]
  */
final case class ChangeBulletRadius(
    actionId: GameAction.Id,
    time: Long,
    bulletId: Entity.Id,
    newRadius: Int,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[Bullet](bulletId, time, _.withRadius(newRadius))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
