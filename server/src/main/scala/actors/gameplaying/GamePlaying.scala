package actors.gameplaying

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import gamecommunication.ServerToClient
import gamelogic.utils.IdGeneratorContainer
import models.menus.{GameJoinedInfo, PlayerName}
import models.menus.GameKeys.GameKey
import gamelogic.utils.Time
import gamelogic.gamestate.gameactions.NewPlayer

/** This is the actor responsible for managing a given game (with the given key).
  *
  * These things need to be done in order:
  *
  *   - wait for everyone to connect, and gather their info.
  */
object GamePlaying {
  sealed trait Command

  case class PlayerConnected(playerName: PlayerName, ref: ActorRef[ConnectionActor.ForExternalWorld]) extends Command
  private case object GameMasterDied extends Command

  def apply(gameKey: GameKey, gameInfo: GameJoinedInfo): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info(s"New game starting ($gameKey)")
    context.log.info(s"""Small summary:
         |Game Key: $gameKey
         |Number of players: ${gameInfo.players.size}
         |Number of teams: ${gameInfo.allTeamIds.size}
         |""".stripMargin)

    receiver(initInfo(gameKey, gameInfo))
  }

  private case class ReceiverInfo(
      gameKey: GameKey,
      gameInfo: GameJoinedInfo,
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld]]
  ) {
    def playerConnected(name: PlayerName, ref: ActorRef[ConnectionActor.ForExternalWorld]): ReceiverInfo =
      copy(players = players + (name -> ref))

    def areAllPlayersConnected: Boolean = gameInfo.allPlayerNames == players.keySet

    def intializedInfo(gameMasterRef: ActorRef[GameMaster.Command]): GameInitialisedInfo =
      GameInitialisedInfo(gameKey, gameInfo, players, gameMasterRef)
  }

  private def initInfo(gameKey: GameKey, gameInfo: GameJoinedInfo): ReceiverInfo =
    ReceiverInfo(gameKey, gameInfo, Map.empty)

  private def receiver(info: ReceiverInfo): Behavior[Command] = Behaviors.receive { (context, command) =>
    command match {
      case PlayerConnected(playerName, ref) =>
        val newInfo = info.playerConnected(playerName, ref)
        if newInfo.areAllPlayersConnected then
          implicit val idGeneratorContainer: IdGeneratorContainer = IdGeneratorContainer.initialIdGeneratorContainer
          val (initialGameState, firstActions)                    = newInfo.gameInfo.initializeGameState
          val players                                             = newInfo.players
          val gameMasterRef =
            context.spawn(GameMaster(idGeneratorContainer, initialGameState, firstActions, players), "GameMaster")
          context.watchWith(gameMasterRef, GameMasterDied)

          val playersIdsByName = firstActions.collect { case newPlayer: NewPlayer =>
            PlayerName(newPlayer.player.name) -> newPlayer.player.id
          }.toMap

          players.foreach { (name, ref) =>
            ref ! ConnectionActor.ServerToClientWrapper(
              ServerToClient.YourEntityIdIs(
                playersIdsByName(name)
              )
            )
          }

          gameInitialised(newInfo.intializedInfo(gameMasterRef))
        else receiver(newInfo)
      case GameMasterDied => Behaviors.unhandled
    }
  }

  private case class GameInitialisedInfo(
      gameKey: GameKey,
      gameInfo: GameJoinedInfo,
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld]],
      gameMaster: ActorRef[GameMaster.Command]
  )

  private def gameInitialised(info: GameInitialisedInfo): Behavior[Command] = Behaviors.ignore

}
