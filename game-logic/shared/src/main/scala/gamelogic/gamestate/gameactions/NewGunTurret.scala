package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.GunTurret
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

final case class NewGunTurret(
    actionId: GameAction.Id,
    time: Long,
    turretId: Entity.Id,
    ownerId: Entity.Id,
    teamId: Int,
    pos: Complex,
    radius: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(
      GunTurret(
        time,
        turretId,
        ownerId,
        teamId,
        pos.re,
        pos.im,
        time,
        radius,
        0,
        GunTurret.maxLifeTotal
      ),
      time
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
