package actors.gameplaying

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import gamecommunication.ServerToClient
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.gamestate.gameactions.*
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer
import models.menus.PlayerName
import gamelogic.utils.Time
import zio.ZIO
import zio.duration.Duration.fromScala
import gamelogic.entities.concreteentities.GameArea
import gamelogic.gamestate.serveractions._

import scala.concurrent.duration.*
import gamelogic.gamestate.{ActionGatherer, GreedyActionGatherer}

import scala.language.implicitConversions

/** The [[GameMaster]] actually runs the game loop and manages the game state.
  */
object GameMaster {

  private def now = System.currentTimeMillis

  /** In millis */
  final val gameLoopTiming = 1000L / 30L

  sealed trait Command

  /** Sent by this actor to itself to run the game loop. We try to keep a rate of 120 per second. */
  private case object GameLoop extends Command

  private case class InternalActions(gameActions: List[GameAction]) extends Command

  case class PlayerDisconnected(playerName: PlayerName) extends Command

  /** Actions were sent by the external world. */
  case class MultipleActionsWrapper(gameActions: List[GameAction], playerName: PlayerName) extends Command

  private def gameLoopTo(to: ActorRef[GameLoop.type], delay: FiniteDuration) =
    for {
      fiber <- zio.clock.sleep(fromScala(delay)).fork
      _     <- fiber.join
      _     <- ZIO.effectTotal(to ! GameLoop)
    } yield ()

  private val serverAction =
    ManageDeadPlayers ++
      ManageTickerBuffs ++
      ManageUsedAbilities ++
      ManageGunTurrets ++
      ManageBullets ++
      ((ManageHealUnits parWith ManageDamageZones) parWith
        (ManageBarriers parWith ManageMists)) ++
      ManageAbilityGivers ++ (ManageSmashBullets parWith ManageHealingZones) ++ ManageBuffsToBeRemoved ++
      ManageEndOfGame

  private implicit def wrapServerToClient(serverToClient: ServerToClient): ConnectionActor.ServerToClientWrapper =
    ConnectionActor.ServerToClientWrapper(serverToClient)

  def apply(
      idGeneratorContainer: IdGeneratorContainer,
      initialGameState: GameState,
      firstActions: List[GameAction],
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]]
  ): Behavior[Command] = Behaviors.setup[Command] { context =>
    context.log.info("Warning all ConnectionActor of my supreme existence.")
    val beginIn = ServerToClient.BeginIn(3000)

    context.log.info(s"Players are $players")
    players.values.foreach(_ ! ConnectionActor.HereIsTheGameMaster(context.self))
    players.values.foreach(_ ! ConnectionActor.ServerToClientWrapper(beginIn))
    players.values.foreach(_ ! ServerToClient.AddAndRemoveActions(firstActions, Time.currentTime(), Nil))

    context.scheduleOnce(beginIn.duration, context.self, GameLoop)

    val initialActionGatherer: ActionGatherer = new GreedyActionGatherer(initialGameState)

    val actionGatherer = initialActionGatherer.slaveAddAndRemoveActions(firstActions, 0, Nil)

    // todo: this should be an argument...
    val gameArea = GameArea(players.size)

    implicit def idGenerator: IdGeneratorContainer = idGeneratorContainer

    Behaviors.receiveMessage {
      case GameLoop =>
        context.log.info("Game about to begin")
        context.self ! InternalActions(List(GameBegins(GameAction.newId(), now, gameArea.gameBounds, ServerSource)))
        context.self ! GameLoop
        gameRunningReceiver(GameRunningInfo(actionGatherer, Nil, idGeneratorContainer, players))
      case _ => Behaviors.unhandled
    }
  }

  private case class GameRunningInfo(
      actionGatherer: ActionGatherer,
      pendingActions: List[GameAction],
      idGeneratorContainer: IdGeneratorContainer,
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]]
  ) {
    implicit def idGenerator: IdGeneratorContainer = idGeneratorContainer

    def addPendingActions(actions: List[GameAction]): GameRunningInfo =
      copy(pendingActions = actions ++ pendingActions)

    def afterGameLoop(newActionGatherer: ActionGatherer): GameRunningInfo =
      copy(pendingActions = Nil, actionGatherer = newActionGatherer)

    def broadcast(serverToClient: ServerToClient): Unit =
      players.values.foreach(_ ! serverToClient)

    def playerDisconnected(playerName: PlayerName): GameRunningInfo =
      copy(players = players - playerName)
  }

  private def gameRunningReceiver(info: GameRunningInfo): Behavior[Command] = Behaviors.receive { (context, command) =>
    import info.idGenerator
    command match {
      case GameLoop =>
        val startTime     = now
        val sortedActions = info.pendingActions.sorted.map(_.setId(GameAction.newId()))

        val sortedActionsWithMaybeErrorMessages =
          sortedActions.map(action => (action, action.isLegal(info.actionGatherer.currentGameState)))

        val (illegalActionsWithMessage, _) = sortedActionsWithMaybeErrorMessages.partitionMap {
          case (action, Some(message)) => Left((action, message))
          case (action, None)          => Right(action)
        }

        illegalActionsWithMessage
          .foreach((action, message) =>
            context.log.warn(s"Received this action $action but is was not legal, message is: $message.")
          )

        try {

          /** First adding actions from entities */
          val (nextCollector, oldestTimeToRemove, idsToRemove) =
            info.actionGatherer.masterAddAndRemoveActions(sortedActions)

          /** Making all the server specific checks */
          val (finalCollector, output) = serverAction(nextCollector, () => System.currentTimeMillis)

          /** Sending outcome back to entities. */
          val finalOutput = ServerAction.ServerActionOutput(
            sortedActions,
            oldestTimeToRemove,
            idsToRemove
          ) merge output
          if finalOutput.createdActions.nonEmpty then
            info.broadcast(
              ServerToClient.AddAndRemoveActions(
                finalOutput.createdActions,
                finalOutput.oldestTimeToRemove,
                finalOutput.idsOfIdsToRemove
              )
            )

          /** Set up for next loop. */
          val timeSpent = now - startTime
          if timeSpent > gameLoopTiming then
            context.self ! GameLoop
            gameRunningReceiver(info.afterGameLoop(finalCollector))
          else
            zio.Runtime.default.unsafeRunToFuture(
              gameLoopTo(context.self, (gameLoopTiming - timeSpent).millis)
            )

            if finalCollector.currentGameState.ended then Behaviors.stopped(() => context.log.info("game ended."))
            else gameRunningReceiver(info.afterGameLoop(finalCollector))

//          if finalCollector.currentGameState.ended && !alreadyClosing then {
//            zio.Runtime.default.unsafeRunToFuture(
//              closeServerAfter(context.self, 1.minute)
//            )
//
//            val endOfGameActions =
//              bossFactory.flatMap(_.whenBossDiesActions(finalCollector.currentGameState, now, idGeneratorContainer))
//
//            val (endOfGameCollector, oldestTimeToRemove, idsToRemove) =
//              actionCollector.masterAddAndRemoveActions(endOfGameActions)
//
//            actionUpdateCollector ! ActionUpdateCollector.GameStateWrapper(endOfGameCollector.currentGameState)
//            actionUpdateCollector ! ActionUpdateCollector
//              .AddAndRemoveActions(
//                endOfGameActions,
//                oldestTimeToRemove,
//                idsToRemove
//              )
//
//            inGameBehaviour(Nil, actionUpdateCollector, endOfGameCollector, bossFactory, alreadyClosing = true)
//          } else {
//            inGameBehaviour(Nil, actionUpdateCollector, finalCollector, bossFactory)
//          }

        } catch {
          case e: Throwable =>
            context.log.error("Error in game loop!", e)
            throw e
        }

      case InternalActions(gameActions) =>
        gameRunningReceiver(info.addPendingActions(gameActions))
      case MultipleActionsWrapper(gameActions, playerName) =>
        val oldestTimeAllowed = now - 1000
        // removing actions that are too old
        val (tooOld, toKeep) = gameActions.partition(_.time < oldestTimeAllowed)
        if tooOld.nonEmpty then
          context.log.warn(
            s"I received these actions but there were too old (time < $oldestTimeAllowed):\n${tooOld.mkString("\n")}"
          )
        // todo: some legality checks
        gameRunningReceiver(info.addPendingActions(toKeep))
      case PlayerDisconnected(playerName) =>
        val newInfo = info.playerDisconnected(playerName)
        if newInfo.players.isEmpty then Behaviors.stopped(() => context.log.info("Everyone is gone"))
        else gameRunningReceiver(newInfo)
    }
  }
}
