package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.SmashBullet
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class SmashBulletGrows(
    actionId: GameAction.Id,
    time: Long,
    smashBulletId: Entity.Id,
    newRadius: Int,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[SmashBullet](smashBulletId, time, _.changeRadius(time, newRadius))

  def changeTime(newTime: Long): SmashBulletGrows = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
