package gamelogic.gamestate.serveractions

import gamelogic.buffs.Buff
import gamelogic.gamestate.gameactions.RemoveBuff
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer
import gamelogic.gamestate.gameactions.RemoveBuff

object ManageBuffsToBeRemoved extends ServerActionFromActionList {

  private def removeBuffAction(actionId: GameAction.Id, buff: Buff): RemoveBuff =
    RemoveBuff(actionId, buff.appearanceTime + buff.duration, buff.bearerId, buff.buffId)

  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    println(getClass)

    val startTime = nowGenerator()
    val gameState = currentState.currentGameState

    val removedBuffs = gameState.allBuffs
      .filter(_.isFinite)
      .filter(buff => startTime - buff.appearanceTime > buff.duration)
      .flatMap { buff =>
        println(s"buff is removed! $buff")
        removeBuffAction(idGeneratorContainer.gameActionIdGenerator(), buff) :: buff.endingAction(gameState, startTime)
      }

    removedBuffs
  }
}
