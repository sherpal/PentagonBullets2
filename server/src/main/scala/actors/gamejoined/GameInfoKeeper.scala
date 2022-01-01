package actors.gamejoined

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.menus.{GameJoinedInfo, PlayerInfo, PlayerName}
import models.syntax.Pointed
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import gamelogic.abilities.Ability

import scala.concurrent.Future

object GameInfoKeeper {
  sealed trait Command

  /** Sent to this actor whenever we want to send a notif. */
  case object SendGameInfo extends Command

  /** Sent to this actor whenever the `respondTo` actor wants the game info */
  case class SendGameInfoTo[T](respondTo: ActorRef[T], adapter: GameJoinedInfo => T) extends Command:
    def send(gameJoinedInfo: GameJoinedInfo): Unit = respondTo ! adapter(gameJoinedInfo)

  case class NewPlayer(playerName: PlayerName) extends Command

  /** Sent to this actor to update (or add) the given [[PlayerInfo]] */
  case class UpdatePlayerAbility(playerName: PlayerName, abilityId: Ability.AbilityId) extends Command

  case class UpdatePlayerReadyStatus(playerName: PlayerName, readyStatus: Boolean) extends Command

  /** Sent to this actor to remove the given [[PlayerName]] */
  case class RemovePlayer(playerName: PlayerName) extends Command

  def apply(notificationRef: ActorRef[GameJoinedInfo]): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info("Setup")

    receiver(Pointed[GameJoinedInfo].unit, notificationRef)
  }

  private def receiver(gameInfo: GameJoinedInfo, notificationRef: ActorRef[GameJoinedInfo]): Behavior[Command] =
    Behaviors.receive { (context, command) =>
      command match {
        case SendGameInfo =>
          context.self ! SendGameInfoTo(respondTo = notificationRef, identity)
          Behaviors.same
        case sendTo: SendGameInfoTo[_] =>
          sendTo.send(gameInfo.obfuscated)
          Behaviors.same
        case NewPlayer(playerName) =>
          context.self ! SendGameInfo
          receiver(gameInfo.withPlayer(PlayerInfo.init(playerName)), notificationRef)
        case UpdatePlayerAbility(playerName, abilityId) =>
          context.self ! SendGameInfo
          receiver(gameInfo.updateAbility(playerName, abilityId), notificationRef)
        case UpdatePlayerReadyStatus(playerName, readyStatus) =>
          context.self ! SendGameInfo
          receiver(gameInfo.updateReadyStatus(playerName, readyStatus), notificationRef)
        case RemovePlayer(playerName) =>
          context.self ! SendGameInfo
          receiver(gameInfo.withoutPlayer(playerName), notificationRef)
      }
    }

}
