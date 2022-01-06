package gamelogic.gamestate.serveractions

import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer

/** Helper [[ServerAction]] when your apply method is simply based on producing a list of [[GameAction]]
  */
trait ServerActionFromActionList extends ServerAction {

  def createActionList(
      currentState: ActionGatherer,
      nowGenerator: () => Long
  )(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction]

  final def apply(
      currentState: ActionGatherer,
      nowGenerator: () => Long
  )(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): (ActionGatherer, ServerAction.ServerActionOutput) = {
    val actions                                  = createActionList(currentState, nowGenerator).toList
    val (nextCollector, oldestTime, idsToRemove) = currentState.masterAddAndRemoveActions(actions)

    (nextCollector, ServerAction.ServerActionOutput(actions, oldestTime, idsToRemove))
  }

}
