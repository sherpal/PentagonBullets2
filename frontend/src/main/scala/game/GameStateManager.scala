package game

import com.raquo.laminar.api.A.*
import gamelogic.entities.Entity
import game.ui.reactivepixi.ReactiveStage
import gamelogic.gamestate.GameState
import gamecommunication.ServerToClient.AddAndRemoveActions
import be.doeraene.physics.Complex
import assets.Asset
import gamecommunication.ClientToServer
import gamelogic.utils.Time
import typings.pixiJs.PIXI.LoaderResource

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
}
