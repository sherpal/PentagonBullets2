package actors.gamejoined

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.menus.*
import server.gamejoined.Routes
import gamelogic.abilities.Ability
import gamelogic.entities.Entity.TeamId
import models.gamecodecs.CirceCodecs.*
import models.menus.GameKeys.GameKey

object ConnectionActor {

  sealed trait Command

  sealed trait ForExternalWorld extends Command {
    def forward: ServerToClient
  }
  case class GameJoinedInfoUpdated(gameJoinedInfo: GameJoinedInfo) extends ForExternalWorld {
    def forward: ServerToClient = ServerToClient.GameInfoWrapper(gameJoinedInfo)
  }
  case class GameStarts(playerInfo: PlayerInfo, gameKey: GameKey) extends ForExternalWorld {
    def forward: ServerToClient = ServerToClient.GameStarts(playerInfo, gameKey)
  }

  case class PlayerNameAlreadyConnected() extends Command
  case class Connected() extends Command

  sealed trait FromExternalWorld extends Command {
    def forward(playerName: PlayerName): GameInfoKeeper.Command
  }
  case class UpdateAbility(abilityId: Ability.AbilityId) extends FromExternalWorld {
    def forward(playerName: PlayerName): GameInfoKeeper.Command =
      GameInfoKeeper.UpdatePlayerAbility(playerName, abilityId)
  }
  case class UpdateReadyStatus(ready: Boolean) extends FromExternalWorld {
    override def forward(playerName: PlayerName): GameInfoKeeper.Command =
      GameInfoKeeper.UpdatePlayerReadyStatus(playerName, ready)
  }
  case class UpdateTeamId(teamId: TeamId) extends FromExternalWorld {
    override def forward(playerName: PlayerName): GameInfoKeeper.Command =
      GameInfoKeeper.UpdatePlayerTeamId(playerName, teamId)
  }
  case class Disconnect() extends FromExternalWorld {
    def forward(playerName: PlayerName): GameInfoKeeper.Command =
      GameInfoKeeper.RemovePlayer(playerName)
  }
  case class StartGame() extends FromExternalWorld {
    def forward(playerName: PlayerName): GameInfoKeeper.Command =
      GameInfoKeeper.StartGame(playerName)
  }

  def fromClientToServer(message: ClientToServer): FromExternalWorld = message match {
    case ClientToServer.SelectAbilityId(abilityId) =>
      UpdateAbility(abilityId)
    case ClientToServer.ChangeReadyStatus(ready) =>
      UpdateReadyStatus(ready)
    case ClientToServer.ChangeTeamId(teamId) =>
      UpdateTeamId(teamId)
    case ClientToServer.Disconnect =>
      Disconnect()
    case ClientToServer.StartGame =>
      StartGame()
  }

  def apply(
      playerName: PlayerName,
      connectionKeeper: ActorRef[ConnectionKeeper.Command],
      gameInfoKeeper: ActorRef[GameInfoKeeper.Command],
      outerWorld: ActorRef[ServerToClient | server.websockethelpers.PoisonPill]
  ): Behavior[Command] =
    Behaviors.setup[Command] { context =>
      connectionKeeper ! ConnectionKeeper.NewConnection(playerName, context.self)

      Behaviors.receiveMessage {
        case msg: ForExternalWorld =>
          if msg.isInstanceOf[GameStarts] then context.log.info("Game starts")
          outerWorld ! msg.forward
          Behaviors.same
        case PlayerNameAlreadyConnected() =>
          context.log.error("This player was already connected, shutting down...")
          outerWorld ! server.websockethelpers.PoisonPill()
          Behaviors.stopped
        case Connected() =>
          context.log.info("Connected")
          gameInfoKeeper ! GameInfoKeeper.NewPlayer(playerName)
          Behaviors.same
        case externalMessage: FromExternalWorld =>
          gameInfoKeeper ! externalMessage.forward(playerName)
          externalMessage match {
            case Disconnect() => Behaviors.stopped
            case _            => Behaviors.same
          }
      }
    }

}
