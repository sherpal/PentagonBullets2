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
import zio.{UIO, ZIO}

import scala.util.{Failure, Success}
import zio.duration.Duration.fromScala
import gamelogic.entities.concreteentities.GameArea
import gamelogic.gamestate.serveractions.*

import scala.concurrent.duration.*
import gamelogic.gamestate.{ActionGatherer, GreedyActionGatherer}
import org.slf4j.{Logger, LoggerFactory}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

/** The [[GameMaster]] actually runs the game loop and manages the game state.
  */
object GameMaster {

  private val globalLogger = LoggerFactory.getLogger(getClass)

  private def now = System.currentTimeMillis

  /** In millis */
  final val gameLoopTiming = 1000L / 120L

  sealed trait Command

  /** Sent by this actor to itself to run the game loop. We try to keep a rate of 120 per second. */
  private case object GameLoop extends Command
  private case object GameEnded extends Command

  private case class InternalActions(gameActions: List[GameAction]) extends Command

  case class PlayerDisconnected(playerName: PlayerName) extends Command

  /** Actions were sent by the external world. */
  case class MultipleActionsWrapper(gameActions: List[GameAction], playerName: PlayerName) extends Command

  private val serverAction =
    new ManageDeadPlayers(true) ++
      ManageTickerBuffs ++
      ManageUsedAbilities ++
      ManageGunTurrets ++
      ManageBullets ++
      ManageHealUnits ++ ManageDamageZones ++
      ManageBarriers ++ ManageMists ++
      ManageAbilityGivers ++ ManageSmashBullets ++ ManageHealingZones ++ ManageBuffsToBeRemoved ++
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
        val queue    = zio.Runtime.default.unsafeRun(zio.Queue.unbounded[GameAction])
        val offerAll = (gameActions: Iterable[GameAction]) => zio.Runtime.default.unsafeRun(queue.offerAll(gameActions))
        context.self ! InternalActions(List(GameBegins(GameAction.newId(), now, gameArea.gameBounds, ServerSource)))
        context.self ! GameLoop
        gameRunningReceiver(
          GameRunningInfo(actionGatherer, Nil, idGeneratorContainer, players, queue, offerAll, () => {})
        )
      case _ => Behaviors.unhandled
    }
  }

  private case class GameRunningInfo(
      actionGatherer: ActionGatherer,
      pendingActions: List[GameAction],
      idGeneratorContainer: IdGeneratorContainer,
      players: Map[PlayerName, ActorRef[ConnectionActor.ForExternalWorld | ConnectionActor.HereIsTheGameMaster]],
      actionQueue: zio.Queue[GameAction],
      offerActions: Iterable[GameAction] => Boolean,
      cancelRunner: () => Unit
  ) {
    implicit def idGenerator: IdGeneratorContainer = idGeneratorContainer

    def ended: Boolean = actionGatherer.currentGameState.ended

    def addPendingActions(actions: List[GameAction]): GameRunningInfo =
      offerActions(actions)
      copy(pendingActions = actions ++ pendingActions)

    def afterGameLoop(newActionGatherer: ActionGatherer): GameRunningInfo =
      copy(pendingActions = Nil, actionGatherer = newActionGatherer)

    def broadcast(serverToClient: ServerToClient): Unit =
      players.values.foreach(_ ! serverToClient)

    def playerDisconnected(playerName: PlayerName): GameRunningInfo =
      copy(players = players - playerName)

    def addCancelRunner(cancelProcedure: () => Unit): GameRunningInfo =
      copy(cancelRunner = () => {
        cancelRunner()
        cancelProcedure()
      })
  }

  private def gameRunningReceiver(info: GameRunningInfo): Behavior[Command] = Behaviors.receive { (context, command) =>
    import info.idGenerator
    command match {
      case GameLoop =>
        implicit def ec: ExecutionContext = context.system.executionContext
        val runner                        = zio.Runtime.default.unsafeRunToFuture(gameLoop(info)(context.log))
        runner onComplete {
          case Success(_) => context.self ! GameEnded
          case Failure(exception) =>
            exception.printStackTrace()
            context.self ! GameEnded
        }

        gameRunningReceiver(
          info.addCancelRunner(() =>
            runner.cancel().onComplete {
              case Failure(exception) =>
                globalLogger.info("Error while cancelling runner, this is bad")
                exception.printStackTrace()
              case Success(_) =>
                globalLogger.info("Error cancelled.")
            }
          )
        )
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
        gameRunningReceiver(info.addPendingActions(toKeep))
      case PlayerDisconnected(playerName) =>
        val newInfo = info.playerDisconnected(playerName)
        if newInfo.players.isEmpty then
          newInfo.cancelRunner()
          Behaviors.stopped(() => context.log.info("Everyone is gone"))
        else gameRunningReceiver(newInfo)
      case GameEnded =>
        context.log.info("Game Ended.")
        Behaviors.stopped
    }
  }

  private def gameLoopTo(to: ActorRef[GameLoop.type], delay: FiniteDuration) =
    for {
      fiber <- zio.clock.sleep(fromScala(delay)).fork
      _     <- fiber.join
      _     <- ZIO.effectTotal(to ! GameLoop)
    } yield ()

  private def gameLoop(info: GameRunningInfo)(implicit
      log: Logger
  ): ZIO[zio.clock.Clock, Throwable, Unit] = (for {
    newActions <- info.actionQueue.takeAll
    (newInfo, theTimeSpent) <- ZIO.effect {
      implicit def idGenerator: IdGeneratorContainer = info.idGeneratorContainer
      val startTime                                  = now
      val sortedActions = newActions
        .map {
          case action: NewBullet =>
            action.copy(id = gamelogic.entities.Entity.newId())
          case other => other
        }
        .map(_.setId(GameAction.newId()))
        .sorted

      val sortedActionsWithMaybeErrorMessages =
        sortedActions.map(action => (action, action.isLegal(info.actionGatherer.currentGameState)))

      val (illegalActionsWithMessage, _) = sortedActionsWithMaybeErrorMessages.partitionMap {
        case (action, Some(message)) => Left((action, message))
        case (action, None)          => Right(action)
      }

      illegalActionsWithMessage
        .foreach((action, message) =>
          log.warn(s"Received this action $action but is was not legal, message is: $message.")
        )

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
      if timeSpent > gameLoopTiming then log.warn(s"Game loop took $timeSpent millis.")
      (info.afterGameLoop(finalCollector), timeSpent)
    }
  } yield (newInfo, theTimeSpent)).flatMap((newInfo, theTimeSpent) =>
    zio.clock.sleep(zio.duration.Duration.fromScala((0L max (gameLoopTiming - theTimeSpent)).millis)) *> gameLoop(
      newInfo
    ).unless(newInfo.ended)
  )

}
