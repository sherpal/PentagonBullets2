package gamelogic.gamestate.serveractions
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer
import gamelogic.gamestate.gameactions.*
import gamelogic.entities.concreteentities.*
import gamelogic.entities.ActionSource.ServerSource

object ManageSmashBullets extends ServerActionFromActionList {
  override def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    println(getClass)

    val time         = nowGenerator()
    val gameState    = currentState.currentGameState
    val smashBullets = gameState.smashBullets.values

    val smashBulletsGoneTooFar = smashBullets
      .filter(bullet => bullet.currentTravelledDistance(time) > SmashBullet.reach)
      .map(bullet => DestroySmashBullet(actionId(), time, bullet.id, ServerSource))

    val growingSmashBullets = smashBullets
      .filter(time - _.lastGrow > SmashBullet.growRate)
      .map { bullet =>
        SmashBulletGrows(actionId(), time, bullet.id, bullet.radius + SmashBullet.growValue, ServerSource)
      }

    val teamsByPlayerId = gameState.teamsByPlayerId
    val hittingSmashBullets = smashBullets.flatMap { bullet =>
      gameState.players.values
        .filterNot(_.team == teamsByPlayerId(bullet.ownerId).teamNbr)
        .find(player => player.collides(bullet, time)) match {
        case Some(player) =>
          List(
            PlayerHitBySmashBullet(actionId(), time, player.id, bullet.id, ServerSource),
            DestroySmashBullet(actionId(), time, bullet.id, ServerSource)
          )
        case None =>
          Nil
      }
    }

    smashBulletsGoneTooFar ++ growingSmashBullets ++ hittingSmashBullets
  }
}
