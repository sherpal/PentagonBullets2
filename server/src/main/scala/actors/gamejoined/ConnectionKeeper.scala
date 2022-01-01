package actors.gamejoined

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import models.menus.{GameJoinedInfo, PlayerName}

object ConnectionKeeper {
  sealed trait Command

  case class GameInfoUpdate(gameInfo: GameJoinedInfo) extends Command
  case class NewConnection(
      playerName: PlayerName,
      ref: ActorRef[ConnectionActor.Command]
  ) extends Command
  case class ConnectionDied(playerName: PlayerName) extends Command

  def apply(parent: ActorRef[GameJoined.Command]): Behavior[Command] = receiver(parent, Map.empty)

  private def receiver(
      parent: ActorRef[GameJoined.Command],
      connectedChildren: Map[PlayerName, ActorRef[ConnectionActor.Command]]
  ): Behavior[Command] = Behaviors.receive { (context, command) =>
    command match {
      case GameInfoUpdate(gameInfo) =>
        connectedChildren.values.foreach(_ ! ConnectionActor.GameJoinedInfoUpdated(gameInfo))
        Behaviors.same
      case NewConnection(playerName, ref) if connectedChildren.contains(playerName) =>
        ref ! ConnectionActor.PlayerNameAlreadyConnected()
        Behaviors.same
      case NewConnection(playerName, ref) =>
        context.log.info(s"New Connection: ${playerName.name}")
        context.watchWith(ref, ConnectionDied(playerName))
        ref ! ConnectionActor.Connected()
        parent ! GameInfoKeeper.SendGameInfoTo(context.self, GameInfoUpdate.apply)
        receiver(parent, connectedChildren = connectedChildren + (playerName -> ref))
      case ConnectionDied(playerName) =>
        context.log.info(s"Connection died: ${playerName.name}")
        receiver(parent, connectedChildren = connectedChildren - playerName)
    }
  }
}