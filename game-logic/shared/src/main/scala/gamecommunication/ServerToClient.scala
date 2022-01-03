package gamecommunication

import boopickle.Default._
import gamelogic.gamestate.GameAction
import gamelogic.entities.Entity

sealed trait ServerToClient

object ServerToClient {

  case class Pong(originalSendingTime: Long, midwayDistantTime: Long) extends ServerToClient

  case class RemoveActions(oldestTime: Long, idsOfActionsToRemove: List[GameAction.Id]) extends ServerToClient

  case class AddAndRemoveActions(
      actionsToAdd: List[GameAction],
      oldestTimeToRemove: Long,
      idsOfActionsToRemove: List[GameAction.Id]
  ) extends ServerToClient

  /** Received just before the beginning of the game so that the client knows what entity they control. */
  case class YourEntityIdIs(entityId: Entity.Id) extends ServerToClient

  given Pickler[ServerToClient] = compositePickler[ServerToClient]
    .addConcreteType[Pong]
    .addConcreteType[RemoveActions]
    .addConcreteType[AddAndRemoveActions]
    .addConcreteType[YourEntityIdIs]

}
