package actors.gameplaying

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import gamecommunication.ServerToClient
import gamelogic.gamestate.{GameAction, GameState}
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

  case class PlayerConnected(
      playerName: PlayerName,
      ref: ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]
  ) extends Command
  case class PlayerIsReady(playerName: PlayerName) extends Command
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

  private case class GameMasterInfo(
      idGeneratorContainer: IdGeneratorContainer,
      initialGameState: GameState,
      firstActions: List[GameAction]
  ) {
    def gameMaster(
        players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]]
    ): Behavior[GameMaster.Command] =
      GameMaster(idGeneratorContainer, initialGameState, firstActions, players)
  }

  private case class ReceiverInfo(
      gameKey: GameKey,
      gameInfo: GameJoinedInfo,
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]],
      readyPlayers: Set[PlayerName],
      maybeGameMasterInfo: Option[GameMasterInfo]
  ) {
    def playerConnected(
        name: PlayerName,
        ref: ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]
    ): ReceiverInfo =
      copy(players = players + (name -> ref))

    def areAllPlayersConnected: Boolean = gameInfo.allPlayerNames == players.keySet

    def intializedInfo(gameMasterRef: ActorRef[GameMaster.Command]): GameInitialisedInfo =
      GameInitialisedInfo(gameKey, gameInfo, players, gameMasterRef)

    def playerIsReady(playerName: PlayerName): ReceiverInfo = copy(readyPlayers = readyPlayers + playerName)

    def areAllPlayersReady: Boolean = gameInfo.allPlayerNames == readyPlayers

    def withGameMasterInfo(info: GameMasterInfo): ReceiverInfo = copy(maybeGameMasterInfo = Some(info))
  }

  private def initInfo(gameKey: GameKey, gameInfo: GameJoinedInfo): ReceiverInfo =
    ReceiverInfo(gameKey, gameInfo, Map.empty, Set.empty, Option.empty)

  private def receiver(info: ReceiverInfo): Behavior[Command] = Behaviors.receive { (context, command) =>
    def players = info.players
    command match {
      case PlayerIsReady(playerName) =>
        val newInfo = info.playerIsReady(playerName)

        if newInfo.areAllPlayersReady then
          val gameMasterInfo = newInfo.maybeGameMasterInfo.get // safe here

          val gameMasterRef = context.spawn(gameMasterInfo.gameMaster(newInfo.players), "GameMaster")
          context.watchWith(gameMasterRef, GameMasterDied)

          gameInitialised(newInfo.intializedInfo(gameMasterRef))
        else receiver(newInfo)
      case PlayerConnected(playerName, ref) =>
        val newInfo = info.playerConnected(playerName, ref)
        if newInfo.areAllPlayersConnected then
          implicit val idGeneratorContainer: IdGeneratorContainer = IdGeneratorContainer.initialIdGeneratorContainer

          val (initialGameState, firstActions) = newInfo.gameInfo.initializeGameState

          val players = newInfo.players

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
          receiver(newInfo.withGameMasterInfo(GameMasterInfo(idGeneratorContainer, initialGameState, firstActions)))
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

  private def gameInitialised(info: GameInitialisedInfo): Behavior[Command] = Behaviors.receive { (context, command) =>
    command match {
      case GameMasterDied =>
        context.log.info("Game master died, shutting down...")
        Behaviors.stopped
      case _ =>
        Behaviors.ignore
    }
  }

}
