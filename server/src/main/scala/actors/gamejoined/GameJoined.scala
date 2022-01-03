package actors.gamejoined

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import gamelogic.abilities.Ability
import models.menus.{GameJoinedInfo, PlayerInfo, PlayerName}
import actors.Implicits.contramap
import actors.gameplaying.GamePlayingKeeper
import models.menus.GameKeys.GameKey

object GameJoined {
  type Command = ConnectionKeeper.Command | GameInfoKeeper.Command | GameStarts

  case class GameStarts(gameJoinedInfo: GameJoinedInfo, gameKey: GameKey)

  def apply(gamePlayingKeeper: ActorRef[GamePlayingKeeper.Command]): Behavior[Command] = Behaviors.setup[Command] {
    implicit context =>
      val connectionKeeper = context.spawn(ConnectionKeeper(context.self), "ConnectionKeeper")
      val notification     = connectionKeeper.contramap[GameJoinedInfo](ConnectionKeeper.GameInfoUpdate.apply)
      val gameInfoKeeper   = context.spawn(GameInfoKeeper(notification, connectionKeeper), "GameInfoKeeper")

      Behaviors.receiveMessage {
        case forConnectionKeeper: ConnectionKeeper.Command =>
          connectionKeeper ! forConnectionKeeper
          Behaviors.same
        case forGameInfoKeeper: GameInfoKeeper.Command =>
          gameInfoKeeper ! forGameInfoKeeper
          Behaviors.same
        case GameStarts(gameJoinedInfo: GameJoinedInfo, gameKey: GameKey) =>
          gamePlayingKeeper ! GamePlayingKeeper.GameStarts(
            gameInfo = gameJoinedInfo,
            gameKey = gameKey,
            warnWhenReady = () => connectionKeeper ! ConnectionKeeper.GameReady(gameJoinedInfo, gameKey)
          )
          Behaviors.same
      }
  }
}
