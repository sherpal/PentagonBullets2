package gamelogic.gamestate.serveractions

import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer
import gamelogic.gamestate.gameactions.*
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.concreteentities.*

object ManageMists extends ServerActionFromActionList {
  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    println(getClass)

    val time               = nowGenerator()
    val gameState          = currentState.currentGameState
    val mists              = gameState.mists
    val gameAreaSideLength = gameState.gameAreaSideLength

    val playersGetHit = mists.values.toList
      .filter(time - _.lastTick > Mist.tickRate)
      .flatMap { mist =>
        UpdateMist(
          actionId(),
          time,
          mist.id,
          mist.lastGrow,
          time,
          mist.sideLength,
          gameAreaSideLength,
          ServerSource
        ) +:
          gameState.players.values.toList
            .filter(player => player.collides(mist, time))
            .map(player => PlayerTakeDamage(actionId(), time, player.id, mist.id, Mist.damagePerTick, ServerSource))
      }

    val growingMists = mists.values
      .filter(_.sideLength > Mist.minGameSide)
      .filter(time - _.lastGrow > Mist.growthRate)
      .map(mist =>
        UpdateMist(
          actionId(),
          time,
          mist.id,
          time,
          mist.lastTick,
          Mist.shrinkFunction(
            time,
            gameState.startTime.get,
            gameAreaSideLength
          ),
          gameAreaSideLength,
          ServerSource
        )
      )

    playersGetHit ++ growingMists
  }
}
