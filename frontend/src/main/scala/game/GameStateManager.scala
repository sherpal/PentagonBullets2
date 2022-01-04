package game

import com.raquo.laminar.api.A.*
import gamelogic.entities.Entity
import game.ui.reactivepixi.ReactiveStage
import gamelogic.gamestate.GameState
import gamecommunication.ServerToClient.AddAndRemoveActions
import be.doeraene.physics.Complex
import assets.Asset
import game.ui.GameDrawer
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

import scala.scalajs.js.timers.{setInterval, setTimeout}
import scala.scalajs.js
import scala.concurrent.duration.*

final class GameStateManager(
    reactiveStage: ReactiveStage,
    initialGameState: GameState,
    actionsFromServerEvents: EventStream[AddAndRemoveActions],
    socketOutWriter: Observer[ClientToServer],
    userControls: UserControls,
    playerId: Entity.Id,
    deltaTimeWithServer: Long,
    resources: PartialFunction[Asset, LoaderResource]
)(implicit owner: Owner) {
  println("Game State Manager initialized")

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
    userControls.$effectiveMousePosition.map(gameDrawer.camera.mousePosToWorld)
      .startWith(Complex.zero)
  val $mouseAngleWithPosition: Signal[Angle] = $gameMousePosition.combineWith($gameStates).map {
    (mousePosition: Complex, gameState: GameState) =>
      val myPositionNow = gameState.players.get(playerId).fold(Complex.zero)(_.pos)
      (mousePosition - myPositionNow).arg
  }

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
    currentTime = () => serverTime
  )

  // todo: add the gui on top

  private var lastTimeStamp = org.scalajs.dom.window.performance.now()

  private val ticker = (_: Double) => {}

  application.ticker.add(ticker)

}
