package gamelogic.entities.concreteentities

import gamelogic.buffs.Buff.ResourceIdentifier
import gamelogic.buffs.{Buff, SimplePassiveBuff}
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.entities.Entity
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.{GunTurretShoots, NewBullet}
import gamelogic.utils.IdGeneratorContainer

final case class BulletGlue(buffId: Buff.Id, appearanceTime: Long, bearerId: Entity.Id, teamId: Int)
    extends SimplePassiveBuff {

  def resourceIdentifier: ResourceIdentifier = Buff.bulletGlue

  val duration: Long = 5000

  def actionTransformer(action: GameAction): List[GameAction] = action match {
    case bullet: NewBullet if bullet.teamId != teamId =>
      List(bullet.copy(speed = bullet.speed / 2))
    case gunTurretShoots: GunTurretShoots if gunTurretShoots.teamId != teamId =>
      List(gunTurretShoots.copy(bulletSpeed = gunTurretShoots.bulletSpeed / 2))
    case _ =>
      List(action)
  }

  def start(gameState: GameState)(implicit idGeneratorContainer: IdGeneratorContainer): Iterable[GameAction] =
    gameState.bullets.values
      .filter(_.teamId != teamId)
      .map { bullet =>
        val newPos         = bullet.currentPosition(appearanceTime)
        val travelledSoFar = bullet.currentTravelledDistance(appearanceTime)

        NewBullet(
          GameAction.newId(),
          bullet.id,
          bullet.ownerId,
          bullet.teamId,
          newPos,
          bullet.radius,
          bullet.direction,
          (bullet.speed / 2).toInt,
          appearanceTime,
          travelledSoFar,
          AbilitySource
        )
      }

  def endingAction(gameState: GameState, time: Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): List[GameAction] = List.empty

}
