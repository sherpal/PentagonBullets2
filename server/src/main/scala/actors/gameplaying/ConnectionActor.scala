package actors.gameplaying

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import gamecommunication.{gameActionIdPickler, ClientToServer, ServerToClient}
import models.menus.PlayerName
import gamelogic.gamestate.GameAction

object ConnectionActor {

  sealed trait Command

  sealed trait FromExternalWorld extends Command

  case class Ping(sendingTime: Long) extends FromExternalWorld
  case object Ready extends FromExternalWorld
  case object ReadyToStart extends FromExternalWorld
  case object Disconnect extends Command
  case class HereIsTheGameMaster(gameMasterRef: ActorRef[GameMaster.Command]) extends Command
  case class GameActionWrapper(actions: List[GameAction]) extends FromExternalWorld

  sealed trait ForExternalWorld extends Command {
    def forward: ServerToClient
  }

  case class ServerToClientWrapper(forward: ServerToClient) extends ForExternalWorld

  def fromClientToServer(clientToServer: ClientToServer): FromExternalWorld = clientToServer match {
    case ClientToServer.Ping(sendingTime)              => Ping(sendingTime)
    case ClientToServer.Ready                          => Ready
    case ClientToServer.ReadyToStart(_)                => ReadyToStart
    case ClientToServer.GameActionWrapper(gameActions) => GameActionWrapper(gameActions)
  }

  type OuterWorldRef = ActorRef[ServerToClient | server.websockethelpers.PoisonPill]

  private case class BehaviorArgs(
      playerName: PlayerName,
      gamePlayingRef: ActorRef[GamePlaying.Command],
      outerWorld: OuterWorldRef
  )

  def apply(
      playerName: PlayerName,
      gamePlayingRef: ActorRef[GamePlaying.Command],
      outerWorld: OuterWorldRef
  ): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info(s"New connection from $playerName")
    initialPingPong(BehaviorArgs(playerName, gamePlayingRef, outerWorld))
  }

  private def initialPingPong(
      behaviorArgs: BehaviorArgs
  ): Behavior[Command] =
    Behaviors.receive { (context, command) =>
      def playerName = behaviorArgs.playerName
      command match {
        case ping: Ping =>
          behaviorArgs.outerWorld ! ServerToClient.Pong(ping.sendingTime, System.currentTimeMillis())
          Behaviors.same
        case Ready =>
          context.log.info(s"Clock for $playerName is synchronized.")
          behaviorArgs.gamePlayingRef ! GamePlaying.PlayerConnected(playerName, context.self)
          playerIsReady(behaviorArgs)
        case _ =>
          Behaviors.unhandled
      }
    }

  private def playerIsReady(behaviorArgs: BehaviorArgs): Behavior[Command] = Behaviors.receive { (context, command) =>
    val playerName     = behaviorArgs.playerName
    val outerWorld     = behaviorArgs.outerWorld
    val gamePlayingRef = behaviorArgs.gamePlayingRef
    command match {
      case msg: ForExternalWorld =>
        outerWorld ! msg.forward
        Behaviors.same
      case Disconnect =>
        Behaviors.stopped
      case ReadyToStart =>
        context.log.info(s"Player $playerName is ready to start!")
        gamePlayingRef ! GamePlaying.PlayerIsReady(playerName)
        Behaviors.same
      case HereIsTheGameMaster(gameMasterRef) =>
        gameMasterIsKnown(gameMasterRef, behaviorArgs)
      case _ =>
        Behaviors.unhandled
    }
  }

  private def gameMasterIsKnown(gameMasterRef: ActorRef[GameMaster.Command], args: BehaviorArgs): Behavior[Command] =
    val playerName = args.playerName
    val outerWorld = args.outerWorld

    Behaviors.receive { (context, command) =>
      command match {
        case GameActionWrapper(actions) =>
          gameMasterRef ! GameMaster.MultipleActionsWrapper(actions, playerName)
          Behaviors.same
        case msg: ForExternalWorld =>
          outerWorld ! msg.forward
          Behaviors.same
        case Disconnect =>
          gameMasterRef ! GameMaster.PlayerDisconnected(playerName)
          Behaviors.same
        case _ =>
          Behaviors.unhandled
      }
    }

}
