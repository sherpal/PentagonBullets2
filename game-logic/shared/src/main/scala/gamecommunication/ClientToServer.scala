package gamecommunication

import boopickle.Default._
import models.menus.PlayerName
import gamelogic.gamestate.GameAction

sealed trait ClientToServer

object ClientToServer {

  case class Ping(sendingTime: Long) extends ClientToServer

  /** Sent when the user is connected and the web socket is open */
  case object Ready extends ClientToServer

  /** Sent when the user received their entity id, and all assets have been loaded. */
  case class ReadyToStart(playerName: PlayerName) extends ClientToServer

  case class GameActionWrapper(gameActions: List[GameAction]) extends ClientToServer

  given Pickler[ClientToServer] = compositePickler[ClientToServer]
    .addConcreteType[Ping]
    .addConcreteType[Ready.type]
    .addConcreteType[ReadyToStart]
    .addConcreteType[GameActionWrapper]
}
