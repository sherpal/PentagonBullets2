package actors.gameplaying

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import gamecommunication.ServerToClient
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer
import models.menus.PlayerName
import gamelogic.utils.Time

import scala.language.implicitConversions

/** The [[GameMaster]] actually runs the game loop and manages the game state.
  */
object GameMaster {
  sealed trait Command

  private implicit def wrapServerToClient(serverToClient: ServerToClient): ConnectionActor.ServerToClientWrapper =
    ConnectionActor.ServerToClientWrapper(serverToClient)

  def apply(
      idGeneratorContainer: IdGeneratorContainer,
      initialGameState: GameState,
      firstActions: List[GameAction],
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]]
  ): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info("Warning all ConnectionActor of my supreme existence.")
    players.values.foreach(_ ! ConnectionActor.HereIsTheGameMaster(context.self))
    players.values.foreach(_ ! ServerToClient.AddAndRemoveActions(firstActions, Time.currentTime(), Nil))

    Behaviors.ignore
  }
}
