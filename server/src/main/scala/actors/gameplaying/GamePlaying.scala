package actors.gameplaying

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import models.menus.GameJoinedInfo
import models.menus.GameKeys.GameKey

object GamePlaying {
  sealed trait Command

  def apply(gameKey: GameKey, gameInfo: GameJoinedInfo): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info(s"New game starting ($gameKey)")
    context.log.info(s"""Small summary:
         |Game Key: $gameKey
         |Number of players: ${gameInfo.players.size}
         |Number of teams: ${gameInfo.allTeamIds.size}
         |""".stripMargin)

    receiver(gameKey, gameInfo)
  }

  private def receiver(gameKey: GameKey, gameInfo: GameJoinedInfo): Behavior[Command] = Behaviors.ignore

}
