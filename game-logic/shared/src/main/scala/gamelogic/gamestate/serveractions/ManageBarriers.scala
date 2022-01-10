package gamelogic.gamestate.serveractions

import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer
import gamelogic.gamestate.gameactions.*
import gamelogic.entities.concreteentities.Barrier
import gamelogic.entities.ActionSource.ServerSource

object ManageBarriers extends ServerActionFromActionList {
  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    val time      = nowGenerator()
    val gameState = currentState.currentGameState
    val barriers  = gameState.barriers

    val barriersDestruction = barriers
      .filter(time - _._2.time > Barrier.lifeTime)
      .keys
      .map(
        DestroyBarrier(actionId(), time, _, ServerSource)
      )

    // the following thing pushes enemy players that would otherwise be stuck on the barrier
    // in a perfect world this would be prevented when the barrier is placed, but because of small
    // lags this might happen.
    val playersRepelling = barriers.values.flatMap { barrier =>
      gameState.players.values
        .filter(_.team != barrier.teamId)
        .filter(_.collides(barrier, time))
        .map(player =>
          (
            player,
            player.firstValidPosition((player.pos - barrier.pos).arg, gameState.collidingPlayerObstacles(player))
          )
        )
        .map { (player, position) =>
          UpdatePlayerPos(
            actionId(),
            time,
            player.id,
            position,
            player.direction,
            moving = false,
            player.rotation,
            ServerSource
          )
        }

    }
    barriersDestruction ++ playersRepelling
  }
}
