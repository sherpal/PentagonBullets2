package gamelogic.gamestate.serveractions

import gamelogic.gamestate.serveractions.ServerAction.ServerActionOutput
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer

trait ServerAction {

  protected def actionId()(implicit idGeneratorContainer: IdGeneratorContainer): GameAction.Id = GameAction.newId()

  def apply(
      currentState: ActionGatherer,
      nowGenerator: () => Long
  )(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): (ActionGatherer, ServerAction.ServerActionOutput)

  /** Isn't this Kleisli? */
  def ++(that: ServerAction): ServerAction = ServerAction.AndThen(this, that)

}

object ServerAction {

  final case class ServerActionOutput(
      createdActions: List[GameAction],
      oldestTimeToRemove: Long,
      idsOfIdsToRemove: List[GameAction.Id]
  ) {
    def merge(that: ServerActionOutput): ServerActionOutput = ServerActionOutput(
      createdActions ++ that.createdActions,
      oldestTimeToRemove min that.oldestTimeToRemove,
      idsOfIdsToRemove ++ that.idsOfIdsToRemove
    )
  }

  private case class AndThen(before: ServerAction, after: ServerAction) extends ServerAction {
    def apply(
        currentState: ActionGatherer,
        nowGenerator: () => Long
    )(implicit
        idGeneratorContainer: IdGeneratorContainer
    ): (ActionGatherer, ServerAction.ServerActionOutput) = {
      val (nextCollector, firstOutput)  = before(currentState, nowGenerator)
      val (lastCollector, secondOutput) = after(nextCollector, nowGenerator)

      (lastCollector, firstOutput merge secondOutput)
    }

  }

}
