package gamelogic.gamestate.serveractions

import gamelogic.buffs.Buff
import gamelogic.entities.concreteentities.Shield
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
    val startTime = nowGenerator()
    val gameState = currentState.currentGameState

    val removedBuffs = gameState.allBuffs
      .filter(_.isFinite)
      .filter(buff => startTime - buff.appearanceTime > buff.duration)
      .flatMap { buff =>
        removeBuffAction(actionId(), buff) :: buff.endingAction(gameState, startTime)
      }

    removedBuffs
  }
}
