package actors.gamejoined

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import gamelogic.abilities.Ability
import models.menus.{GameJoinedInfo, PlayerInfo, PlayerName}
import actors.Implicits.contramap

object GameJoined {
  type Command = ConnectionKeeper.Command | GameInfoKeeper.Command

  def apply(): Behavior[Command] = Behaviors.setup[Command] { implicit context =>
    val connectionKeeper = context.spawn(ConnectionKeeper(context.self), "ConnectionKeeper")
    val notification     = connectionKeeper.contramap[GameJoinedInfo](ConnectionKeeper.GameInfoUpdate.apply)
    val gameInfoKeeper   = context.spawn(GameInfoKeeper(notification), "GameInfoKeeper")

    Behaviors.receiveMessage {
      case forConnectionKeeper: ConnectionKeeper.Command =>
        connectionKeeper ! forConnectionKeeper
        Behaviors.same
      case forGameInfoKeeper: GameInfoKeeper.Command =>
        gameInfoKeeper ! forGameInfoKeeper
        Behaviors.same
    }
  }
}
