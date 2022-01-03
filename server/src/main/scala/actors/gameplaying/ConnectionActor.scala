package actors.gameplaying

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import gamecommunication.{ClientToServer, ServerToClient}
import models.menus.PlayerName

object ConnectionActor {

  sealed trait Command

  sealed trait FromExternalWorld extends Command

  case class Ping(sendingTime: Long) extends FromExternalWorld
  case object Ready extends FromExternalWorld
  case object ReadyToStart extends FromExternalWorld

  def fromClientToServer(clientToServer: ClientToServer): FromExternalWorld = clientToServer match {
    case ClientToServer.Ping(sendingTime)              => Ping(sendingTime)
    case ClientToServer.Ready                          => Ready
    case ClientToServer.ReadyToStart(_)                => ReadyToStart
    case ClientToServer.GameActionWrapper(gameActions) => ???
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
          playerIsReady(behaviorArgs)
        case _ =>
          Behaviors.unhandled
      }
    }

  private def playerIsReady(behaviorArgs: BehaviorArgs): Behavior[Command] = ???

}
