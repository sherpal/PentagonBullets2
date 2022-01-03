package actors.gameplaying

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import models.menus.GameJoinedInfo
import models.menus.GameKeys.GameKey

/** The [[GamePlayingKeeper]] is the guy to contact when a game needs to start.
  *
  * It will spawn a new [[GamePlaying]] and warn when it's done. Then, anyone can ask for the reference to a particular
  * [[GamePlaying]] based on the [[GameKey]].
  */
object GamePlayingKeeper {

  sealed trait Command

  /** Sent by the [[actors.gamejoined.GameJoined]] when a new game starts. */
  case class GameStarts(gameInfo: GameJoinedInfo, gameKey: GameKey, warnWhenReady: () => Unit) extends Command

  /** Sent by the Routes to know what actor is responsible for this game. */
  case class GamePlayingRefPlease(gameKey: GameKey, replyTo: ActorRef[Option[ActorRef[GamePlaying.Command]]])
      extends Command

  /** Sent as watch command for a child. */
  private case class GameEnded(key: GameKey) extends Command

  def apply(): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info("Setup")
    receiver(Map.empty)
  }

  private def receiver(children: Map[GameKey, ActorRef[GamePlaying.Command]]): Behavior[Command] =
    Behaviors.receive { (context, command) =>
      command match {
        case GameStarts(gameInfo, gameKey, warnWhenReady) =>
          val child = context.spawn(GamePlaying(gameKey, gameInfo), "GamePlaying" ++ gameKey.toString)
          context.watchWith(child, GameEnded(gameKey))
          warnWhenReady()
          receiver(children + (gameKey -> child))
        case GamePlayingRefPlease(gameKey, replyTo) =>
          replyTo ! children.get(gameKey)
          Behaviors.same
        case GameEnded(key) =>
          receiver(children - key)
      }
    }

}
