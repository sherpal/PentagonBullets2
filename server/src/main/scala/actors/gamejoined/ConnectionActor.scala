package actors.gamejoined

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.menus.*
import server.gamejoined.Routes
import gamelogic.abilities.Ability
import models.gamecodecs.CirceCodecs._

object ConnectionActor {

  sealed trait Command
  sealed trait FromExternalWorld extends Command
  case class GameJoinedInfoUpdated(gameJoinedInfo: GameJoinedInfo) extends Command
  case class PlayerNameAlreadyConnected() extends Command
  case class Connected() extends Command
  case class UpdateAbility(abilityId: Ability.AbilityId) extends FromExternalWorld
  case class UpdateReadyStatus(ready: Boolean) extends FromExternalWorld
  case class Disconnect() extends FromExternalWorld

  def fromClientToServer(message: ClientToServer): FromExternalWorld = message match {
    case ClientToServer.SelectAbilityId(abilityId) =>
      UpdateAbility(abilityId)
    case ClientToServer.ChangeReadyStatus(ready) =>
      UpdateReadyStatus(ready)
    case ClientToServer.Disconnect =>
      Disconnect()
  }

  def apply(
      playerName: PlayerName,
      connectionKeeper: ActorRef[ConnectionKeeper.Command],
      gameInfoKeeper: ActorRef[GameInfoKeeper.Command],
      outerWorld: ActorRef[GameJoinedInfo | server.websockethelpers.PoisonPill]
  ): Behavior[Command] =
    Behaviors.setup[Command] { context =>
      connectionKeeper ! ConnectionKeeper.NewConnection(playerName, context.self)

      Behaviors.receiveMessage {
        case GameJoinedInfoUpdated(gameJoinedInfo) =>
          outerWorld ! gameJoinedInfo
          Behaviors.same
        case PlayerNameAlreadyConnected() =>
          context.log.error("This player was already connected, shutting down...")
          outerWorld ! server.websockethelpers.PoisonPill()
          Behaviors.stopped
        case Connected() =>
          context.log.info("Connected")
          gameInfoKeeper ! GameInfoKeeper.NewPlayer(playerName)
          Behaviors.same
        case UpdateAbility(abilityId) =>
          gameInfoKeeper ! GameInfoKeeper.UpdatePlayerAbility(playerName, abilityId)
          Behaviors.same
        case UpdateReadyStatus(ready) =>
          gameInfoKeeper ! GameInfoKeeper.UpdatePlayerReadyStatus(playerName, ready)
          Behaviors.same
        case Disconnect() =>
          gameInfoKeeper ! GameInfoKeeper.RemovePlayer(playerName)
          Behaviors.stopped
      }
    }

}
