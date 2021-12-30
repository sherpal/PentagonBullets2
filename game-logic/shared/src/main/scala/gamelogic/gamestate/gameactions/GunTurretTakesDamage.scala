package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.GunTurret
import gamelogic.gamestate.statetransformers.{GameStateTransformer, TransformEntity}
import gamelogic.gamestate.{GameAction, GameState}

final case class GunTurretTakesDamage(
    actionId: GameAction.Id,
    time: Long,
    turretId: Entity.Id,
    damage: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[GunTurret](turretId, time, _.takesDamage(damage))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
