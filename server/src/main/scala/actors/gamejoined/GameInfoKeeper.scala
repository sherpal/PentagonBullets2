package actors.gamejoined

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.menus.{GameJoinedInfo, PlayerInfo, PlayerName}
import models.syntax.Pointed
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import gamelogic.abilities.Ability
import gamelogic.entities.Entity.TeamId

import scala.concurrent.Future

object GameInfoKeeper {
  sealed trait Command

  /** Sent to this actor whenever we want to send a notif. */
  case object SendGameInfo extends Command

  /** Sent to this actor whenever the `respondTo` actor wants the game info */
  case class SendGameInfoTo[T](respondTo: ActorRef[T], adapter: GameJoinedInfo => T) extends Command:
    def send(gameJoinedInfo: GameJoinedInfo): Unit = respondTo ! adapter(gameJoinedInfo)

  sealed trait GameInfoUpdater extends Command {
    def updateGameInfo(gameJoinedInfo: GameJoinedInfo): GameJoinedInfo
  }

  /** Sent to this actor to add a new player to the game. */
  case class NewPlayer(playerName: PlayerName) extends GameInfoUpdater {
    def updateGameInfo(gameJoinedInfo: GameJoinedInfo): GameJoinedInfo =
      gameJoinedInfo.withPlayer(PlayerInfo.init(playerName, gameJoinedInfo.firstUnusedTeamId))
  }

  /** Sent to this actor to update the [[Ability.AbilityId]] of the given player. */
  case class UpdatePlayerAbility(playerName: PlayerName, abilityId: Ability.AbilityId) extends GameInfoUpdater {
    def updateGameInfo(gameJoinedInfo: GameJoinedInfo): GameJoinedInfo =
      gameJoinedInfo.updateAbility(playerName, abilityId)
  }

  /** Sent to this actor to update the ready status of the given player. */
  case class UpdatePlayerReadyStatus(playerName: PlayerName, readyStatus: Boolean) extends GameInfoUpdater {
    def updateGameInfo(gameJoinedInfo: GameJoinedInfo): GameJoinedInfo =
      gameJoinedInfo.updateReadyStatus(playerName, readyStatus)
  }

  /** Sent to this actor to update the [[TeamId]] of the given player. */
  case class UpdatePlayerTeamId(playerName: PlayerName, teamId: TeamId) extends GameInfoUpdater {
    def updateGameInfo(gameJoinedInfo: GameJoinedInfo): GameJoinedInfo =
      gameJoinedInfo.updateTeamId(playerName, teamId)
  }

  /** Sent to this actor to remove the given [[PlayerName]] */
  case class RemovePlayer(playerName: PlayerName) extends GameInfoUpdater {
    def updateGameInfo(gameJoinedInfo: GameJoinedInfo): GameJoinedInfo =
      gameJoinedInfo.withoutPlayer(playerName)
  }

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
        case updater: GameInfoUpdater =>
          context.self ! SendGameInfo
          receiver(updater.updateGameInfo(gameInfo), notificationRef)
      }
    }

}
