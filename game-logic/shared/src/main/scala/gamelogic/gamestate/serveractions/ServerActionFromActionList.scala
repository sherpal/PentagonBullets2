package gamelogic.gamestate.serveractions

import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer

import scala.collection.parallel.CollectionConverters._

/** Helper [[ServerAction]] when your apply method is simply based on producing a list of [[GameAction]]
  */
trait ServerActionFromActionList extends ServerAction {

  def createActionList(
      currentState: ActionGatherer,
      nowGenerator: () => Long
  )(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction]

  def parWith(that: ServerActionFromActionList): ServerActionFromActionList =
    ServerActionFromActionList.Par(this, that)

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

object ServerActionFromActionList {

  // todo: actually do this in parallel.
  private case class Par(left: ServerActionFromActionList, right: ServerActionFromActionList)
      extends ServerActionFromActionList {
    def createActionList(
        currentState: ActionGatherer,
        nowGenerator: () => Long
    )(implicit
        idGeneratorContainer: IdGeneratorContainer
    ): Iterable[GameAction] =
      List(left, right).par.map(_.createActionList(currentState, nowGenerator)).flatten.toList
  }

}
