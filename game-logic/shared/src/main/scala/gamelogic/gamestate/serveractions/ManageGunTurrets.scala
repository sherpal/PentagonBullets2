package gamelogic.gamestate.serveractions

import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.gamestate.gameactions.*
import gamelogic.utils.IdGeneratorContainer
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.Entity

object ManageGunTurrets extends ServerAction {

  def apply(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): (ActionGatherer, ServerAction.ServerActionOutput) = {
    val now             = nowGenerator()
    val gameState       = currentState.currentGameState
    val teamsByPlayerId = gameState.teamsByPlayerId
    val actions: List[GameAction] = gameState.allTEntities[Bullet].values.toList.flatMap { bullet =>
      gameState
        .allTEntities[GunTurret]
        .values
        .filterNot(turret => teamsByPlayerId(bullet.ownerId).contains(turret.ownerId))
        .find(turret => turret.collides(bullet, now))
        .toList
        .flatMap { turret =>
          val damage = Bullet.damageFromRadius(bullet.radius)
          if turret.lifeTotal <= damage then
            List(
              DestroyBullet(GameAction.newId(), bullet.id, now, ServerSource),
              DestroyGunTurret(GameAction.newId(), now, turret.id, ServerSource)
            )
          else
            List(
              DestroyBullet(GameAction.newId(), bullet.id, now, ServerSource),
              GunTurretTakesDamage(GameAction.newId(), now, turret.id, damage, ServerSource)
            )
        }
    } ++ gameState
      .allTEntities[GunTurret]
      .values
      .filter(now - _.lastShot > GunTurret.shootRate)
      .flatMap { turret =>
        gameState.players.values
          .filterNot(teamsByPlayerId(turret.ownerId).contains)
          .map(player => (player, turret.pos distanceTo player.currentPosition(now)))
          .filter(_._2 < GunTurret.defaultReach * GunTurret.defaultReach)
          .minByOption(_._2)
          .map { (target, _) =>
            val rotation = (target.pos - turret.pos).arg

            GunTurretShoots(
              GameAction.newId(),
              now,
              turret.id,
              rotation,
              Entity.newId(),
              Bullet.defaultRadius,
              Bullet.speed,
              ServerSource
            )
          }
      }

    val (nextCollector, oldestTime, idsToRemove) = currentState.masterAddAndRemoveActions(actions)

    (nextCollector, ServerAction.ServerActionOutput(actions, oldestTime, idsToRemove))
  }
}
