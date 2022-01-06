package gamecommunication

import boopickle.Default._
import gamelogic.gamestate.GameAction
import gamelogic.entities.Entity

import scala.concurrent.duration._

sealed trait ServerToClient

object ServerToClient {

  case object Heartbeat extends ServerToClient

  case class Pong(originalSendingTime: Long, midwayDistantTime: Long) extends ServerToClient

  case class RemoveActions(oldestTime: Long, idsOfActionsToRemove: List[GameAction.Id]) extends ServerToClient

  case class AddAndRemoveActions(
      actionsToAdd: List[GameAction],
      oldestTimeToRemove: Long,
      idsOfActionsToRemove: List[GameAction.Id]
  ) extends ServerToClient

  case class BeginIn(millis: Long) extends ServerToClient {
    def duration: FiniteDuration = millis.millis
  }

  /** Received just before the beginning of the game so that the client knows what entity they control. */
  case class YourEntityIdIs(entityId: Entity.Id) extends ServerToClient

  given Pickler[ServerToClient] = compositePickler[ServerToClient]
    .addConcreteType[Pong]
    .addConcreteType[RemoveActions]
    .addConcreteType[AddAndRemoveActions]
    .addConcreteType[YourEntityIdIs]
    .addConcreteType[Heartbeat.type]
    .addConcreteType[BeginIn]

}
