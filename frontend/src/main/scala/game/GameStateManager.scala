package game

import com.raquo.laminar.api.A.*
import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.Player
import game.ui.reactivepixi.ReactiveStage
import gamelogic.gamestate.GameState
import gamecommunication.ServerToClient.{AddAndRemoveActions, BeginIn}
import be.doeraene.physics.Complex
import assets.Asset
import game.ui.GameDrawer
import game.ui.gui.ReactiveGUIDrawer
import gamecommunication.ClientToServer
import gamelogic.utils.Time
import typings.pixiJs.PIXI.LoaderResource
import gamelogic.gamestate.gameactions.UpdatePlayerPos
import typings.pixiJs.mod.Application
import gamelogic.gamestate.*
import gamelogic.entities.WithPosition.Angle
import models.playing.UserInput
import gamelogic.abilities.Ability
import utils.pixi.monkeypatching.PIXIPatching.TickerWithDoubleAdd
import gamelogic.entities.ActionSource.PlayerSource
import org.scalajs.dom

import scala.scalajs.js.timers.{setInterval, setTimeout}
import scala.scalajs.js
import scala.concurrent.duration.*

final class GameStateManager(
    reactiveStage: ReactiveStage,
    initialGameState: GameState,
    actionsFromServerEvents: EventStream[AddAndRemoveActions],
    beginInEvents: EventStream[BeginIn],
    socketOutWriter: Observer[ClientToServer],
    userControls: UserControls,
    playerId: Entity.Id,
    deltaTimeWithServer: Long,
    resources: PartialFunction[Asset, LoaderResource]
)(implicit owner: Owner) {

  dom.console.info(s"Your id is $playerId.")

  beginInEvents.foreach(beginIn => dom.console.info(s"Game Begins in ${beginIn.millis} millis"))

  @inline def serverTime: Long         = System.currentTimeMillis() + deltaTimeWithServer
  @inline def application: Application = reactiveStage.application

  var maybeLastPositionUpdate: Option[UpdatePlayerPos] = Option.empty

  setInterval(100.millis) {
    maybeLastPositionUpdate.foreach { action =>
      socketOutWriter.onNext(ClientToServer.GameActionWrapper(List(action)))
    }
    maybeLastPositionUpdate = None
  }

  private val gameStateUpdatesBus: EventBus[(GameState, Long)] = new EventBus[(GameState, Long)]
  val gameStateUpdates: EventStream[(GameState, Long)]         = gameStateUpdatesBus.events

  private var actionCollector: ActionGatherer = new GreedyActionGatherer(initialGameState)

  val gameDrawer = new GameDrawer(reactiveStage, resources)

  private val gameStateBus: EventBus[GameState] = new EventBus[GameState]

  val $gameStates: Signal[GameState] = gameStateBus.events.startWith(initialGameState)

  /** Signal giving at all time the game position of the user mouse.
    */
  val $gameMousePosition: Signal[Complex] =
    userControls.$effectiveMousePosition.map(gameDrawer.camera.mousePosToWorld).startWith(Complex.zero)

  private var unconfirmedActions: List[GameAction] = Nil

  private def nextGameState(): Unit =
    gameStateBus.writer.onNext(actionCollector.currentGameState.applyActions(unconfirmedActions))

  actionsFromServerEvents.foreach { case AddAndRemoveActions(actionsToAdd, oldestTimeToRemove, idsOfActionsToRemove) =>
    actionCollector = actionCollector.slaveAddAndRemoveActions(actionsToAdd, oldestTimeToRemove, idsOfActionsToRemove)

    unconfirmedActions = unconfirmedActions.lastOption.toList

    setTimeout(1) {
      actionsToAdd
        .filterNot(action => idsOfActionsToRemove.contains(action.actionId))
        .foreach(newActionsBus.writer.onNext)
    }

    nextGameState()
  }

  private val newActionsBus: EventBus[GameAction] = new EventBus[GameAction]

  val $actionsWithStates: EventStream[(GameAction, GameState)] = newActionsBus.events.withCurrentValueOf($gameStates)

  val pressedUserInputSignal: Signal[Set[UserInput]] = userControls.$pressedUserInput

  val useAbilityBus = new EventBus[Ability.AbilityId]

  val abilityHandler = new CastAbilitiesHandler(
    playerId,
    userControls = userControls,
    $gameStates = $gameStates,
    $gameMousePosition = $gameMousePosition,
    socketOutWriter = socketOutWriter,
    useAbilityEvents = useAbilityBus.events,
    gameDrawer = gameDrawer,
    deltaTimeWithServer = deltaTimeWithServer,
    currentTime = () => serverTime,
    setUnconfirmedActions = unconfirmedActions = _
  )

  val guiDrawer = new ReactiveGUIDrawer(playerId, reactiveStage, resources, useAbilityBus.writer, gameStateUpdates)

  private var lastTimeStamp = org.scalajs.dom.window.performance.now()

  private val gameLoopInfoSignal = $gameStates.combineWith($gameMousePosition, userControls.$pressedUserInput)
    .map((gameState, mousePos, pressedKeys) =>
      (gameState, gameState.entityByIdAs[Player](playerId), mousePos, pressedKeys)
    )
    .observe

  private val gameStateStrictSignal = $gameStates.observe

  def movePlayer(
      gameState: GameState,
      time: Long,
      dt: Double,
      mousePos: Complex,
      player: Player,
      headingTo: Complex
  ): UpdatePlayerPos = {
    val rotation            = (mousePos - player.pos).arg
    val (moving, direction) = if headingTo == Complex.zero then (false, 0.0) else (true, headingTo.arg)
    val obstaclesLike       = gameState.collidingPlayerObstacles(player)

    val newPosition = if moving then {
      val pos = player.lastValidPosition(
        player.pos + player.speed * dt / 1000 * Complex.rotation(direction),
        obstaclesLike
      )

      if pos != player.pos then pos
      else {
        val secondTry = player.lastValidPosition(
          player.pos + player.speed * dt / 1000 * Complex.rotation(direction - math.Pi / 4),
          obstaclesLike
        )
        if secondTry != player.pos then secondTry
        else
          player.lastValidPosition(
            player.pos + player.speed * dt / 1000 * Complex.rotation(direction + math.Pi / 4),
            obstaclesLike
          )
      }
    } else player.pos

    val newRotation =
      if obstaclesLike.exists(obstacle =>
          player.shape.collides(newPosition, rotation, obstacle.shape, obstacle.pos, obstacle.rotation)
        )
      then player.rotation
      else rotation

    val finalMoving = player.pos != newPosition

    UpdatePlayerPos(
      GameAction.Id.initial,
      time,
      playerId,
      newPosition,
      (newPosition - player.pos).arg,
      finalMoving,
      newRotation,
      PlayerSource
    )
  }

  private val ticker = (_: Double) => {
    nextGameState() // need for last unconfirmed actions to kick in
    val info = gameLoopInfoSignal.now()

    val (gameState: GameState, maybePlayer, mousePos, pressedUserInput) = info

    maybePlayer match {
      case Some(me) =>
        val now       = serverTime
        val deltaTime = now - lastTimeStamp
        lastTimeStamp = now.toDouble

        if gameState.isPlaying then {
          val playerMovement = UserInput.movingDirection(pressedUserInput)
          val positionAction = movePlayer(gameState, now, deltaTime, mousePos, me, playerMovement)

          unconfirmedActions = unconfirmedActions :+ positionAction
          maybeLastPositionUpdate = Some(positionAction)

          nextGameState()
        }
      case None if gameState.started =>
        val cameraSize = gameState.mists.values.headOption match {
          case None       => gameState.gameAreaSideLength.toDouble
          case Some(mist) => mist.sideLength * 1.1
        }

        val widthToHeightRatio = gameDrawer.camera.worldWidth / gameDrawer.camera.worldHeight

        if widthToHeightRatio > 1 then
          gameDrawer.camera.worldWidth = cameraSize * widthToHeightRatio
          gameDrawer.camera.worldHeight = cameraSize
        else
          gameDrawer.camera.worldWidth = cameraSize
          gameDrawer.camera.worldHeight = cameraSize / widthToHeightRatio
      case None => // nothing to do
    }

    val now             = serverTime
    val gameStateToDraw = gameStateStrictSignal.now()
    gameDrawer.drawGameState(
      gameStateToDraw,
      gameStateToDraw.entityByIdAs[Player](playerId).fold(Complex.zero)(_.currentPosition(now)),
      now
    )

    gameStateUpdatesBus.writer.onNext((gameStateToDraw, now))
  }

  application.ticker.add(ticker)

}
