package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.{Bullet, GunTurret}
import gamelogic.gamestate.statetransformers.{GameStateTransformer, TransformEntity, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

final case class GunTurretShoots(
    actionId: GameAction.Id,
    time: Long,
    turretId: Entity.Id,
    rotation: Double,
    bulletId: Entity.Id,
    bulletRadius: Int,
    bulletSpeed: Double,
    actionSource: ActionSource
) extends GameAction {

  private def withBullet: GameStateTransformer =
    GameStateTransformer.fromOption(_.entityByIdAs[GunTurret](turretId))(turret =>
      WithEntity(
        Bullet(
          bulletId,
          time,
          turret.ownerId,
          turret.teamId,
          turret.xPos,
          turret.yPos,
          bulletRadius,
          rotation,
          bulletSpeed,
          0
        ),
        time
      )
    )

  override def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    withBullet ++ TransformEntity[GunTurret](turretId, time, _.shootTowards(rotation, time))

  def applyDefault(gameState: GameState): GameState = {
    val turret = gameState.entityByIdAs[GunTurret](turretId).get
    gameState
      .withBullet(
        bulletId,
        time,
        Bullet(
          bulletId,
          time,
          turret.ownerId,
          turret.teamId,
          turret.xPos,
          turret.yPos,
          bulletRadius,
          rotation,
          bulletSpeed,
          0
        )
      )
      .withGunTurret(
        turretId,
        time,
        GunTurret(
          turret.creationTime,
          turretId,
          turret.ownerId,
          turret.teamId,
          turret.xPos,
          turret.yPos,
          time,
          turret.radius,
          rotation,
          turret.lifeTotal
        )
      )
  }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
