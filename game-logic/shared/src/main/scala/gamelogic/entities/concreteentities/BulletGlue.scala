package gamelogic.entities.concreteentities

import gamelogic.buffs.Buff.ResourceIdentifier
import gamelogic.buffs.{Buff, PassiveBuff}
import gamelogic.entities.Entity
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.NewBullet
import gamelogic.utils.IdGeneratorContainer

final case class BulletGlue(buffId: Buff.Id, appearanceTime: Long, bearerId: Entity.Id, teamId: Int)
    extends PassiveBuff {

  def resourceIdentifier: ResourceIdentifier = Buff.bulletGlue

  val duration: Long = 5000

  def actionTransformer(action: GameAction): List[GameAction] = action match {
    case bullet: NewBullet if bullet.teamId != teamId =>
      List(bullet.copy(speed = bullet.speed / 2))
    case _ =>
      List(action)
  }

  def start(gameState: GameState): GameState =
    gameState
      .allTEntities[Bullet]
      .values
      .filter(_.teamId != teamId)
      .foldLeft(gameState) { case (gs, bullet) =>
        val newPos = bullet.currentPosition(appearanceTime - bullet.time)
        gs.withBullet(
          bullet.id,
          appearanceTime,
          Bullet(
            bullet.id,
            appearanceTime,
            bullet.ownerId,
            bullet.teamId,
            newPos.re,
            newPos.im,
            bullet.radius,
            bullet.direction,
            bullet.speed / 2,
            bullet.currentTravelledDistance(appearanceTime)
          )
        )
      }

  def endingAction(gameState: GameState, time: Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): List[GameAction] = List.empty

}
