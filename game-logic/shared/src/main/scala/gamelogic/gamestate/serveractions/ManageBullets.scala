package gamelogic.gamestate.serveractions

import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.gamestate.gameactions.*
import gamelogic.utils.IdGeneratorContainer
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.Entity

object ManageBullets extends ServerActionFromActionList {
  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    println(getClass)

    val time      = nowGenerator()
    val gameState = currentState.currentGameState

    // killing bullets that went to far or hit an obstacle
    val (deadBullets, aliveBullets) = gameState.bullets.values.toList
      .partition { bullet =>
        gameState
          .collidingPlayerObstacles(bullet.teamId)
          .exists(_.collides(bullet, time)) || bullet.currentTravelledDistance(time) > Bullet.reach
      }

    val deadBulletsActions = deadBullets.map(bullet => DestroyBullet(actionId(), bullet.id, time, ServerSource))

    val playerHitActions = gameState.players.values.toList
      .map(player => (player, player.currentPosition(time)))
      .map { (player, playerPos) =>
        (
          aliveBullets
            .filter(bullet => bullet.teamId != player.team)
            .filter { bullet =>
              bullet.shape.collides(
                bullet.currentPosition(time),
                0,
                player.shape,
                playerPos,
                player.rotation
              )
            },
          player.id
        )
      }
      .filter(_._1.nonEmpty)
      .map { case (collidingBullets, playerId) =>
        PlayerHitByMultipleBullets(
          actionId(),
          time,
          collidingBullets.map(_.id),
          playerId,
          collidingBullets.map(bullet => Bullet.damageFromRadius(bullet.radius)).sum,
          ServerSource
        )
      }

    deadBulletsActions ++ playerHitActions
  }
}
