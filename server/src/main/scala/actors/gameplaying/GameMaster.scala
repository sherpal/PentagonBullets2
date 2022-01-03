package actors.gameplaying

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer
import models.menus.PlayerName

/** The [[GameMaster]] actually runs the game loop and manages the game state.
  */
object GameMaster {
  sealed trait Command

  def apply(
      idGeneratorContainer: IdGeneratorContainer,
      initialGameState: GameState,
      firstActions: List[GameAction],
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld]]
  ): Behavior[Command] = Behaviors.ignore
}
