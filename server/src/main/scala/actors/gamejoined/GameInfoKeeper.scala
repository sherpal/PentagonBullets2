package actors.gamejoined

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.menus.{GameJoinedInfo, PlayerInfo, PlayerName}
import models.syntax.Pointed
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import gamelogic.abilities.Ability
import gamelogic.entities.Entity.TeamId
import models.menus.GameKeys.GameKey

import scala.concurrent.Future

object GameInfoKeeper {
  sealed trait Command

  /** Sent to this actor whenever we want to send a notif. */
  case object SendGameInfo extends Command

  /** Sent to this actor whenever the `respondTo` actor wants the game info */
  case class SendGameInfoTo[T](respondTo: ActorRef[T], adapter: GameJoinedInfo => T) extends Command:
    def send(gameJoinedInfo: GameJoinedInfo): Unit = respondTo ! adapter(gameJoinedInfo)

  /** Sent to this actor when the game leader starts the game. */
  case class StartGame(requester: PlayerName) extends Command

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

  def apply(
      notificationRef: ActorRef[GameJoinedInfo],
      connectionKeeper: ActorRef[ConnectionKeeper.GameStarts]
  ): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info("Setup")
    clean(notificationRef, connectionKeeper)
  }

  private def clean(
      notificationRef: ActorRef[GameJoinedInfo],
      connectionKeeper: ActorRef[ConnectionKeeper.GameStarts]
  ) =
    receiver(Pointed[GameJoinedInfo].unit, notificationRef, connectionKeeper)

  private def receiver(
      gameInfo: GameJoinedInfo,
      notificationRef: ActorRef[GameJoinedInfo],
      connectionKeeper: ActorRef[ConnectionKeeper.GameStarts]
  ): Behavior[Command] =
    Behaviors.receive { (context, command) =>
      command match {
        case SendGameInfo =>
          context.self ! SendGameInfoTo(respondTo = notificationRef, identity)
          Behaviors.same
        case sendTo: SendGameInfoTo[_] =>
          sendTo.send(gameInfo)
          Behaviors.same
        case updater: GameInfoUpdater =>
          context.self ! SendGameInfo
          receiver(updater.updateGameInfo(gameInfo), notificationRef, connectionKeeper)
        case StartGame(requester) =>
          if gameInfo.isLeader(requester) && gameInfo.canStart then
            context.log.info("Game starting...")
            connectionKeeper ! ConnectionKeeper.GameStarts(gameInfo, GameKey.random())
            clean(notificationRef, connectionKeeper)
          else
            context.log.warn(
              "Received StartGame request but either the game can't start, or it was not from the leader."
            )
            Behaviors.same
      }
    }

}
