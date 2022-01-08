package game.ui.gui

import assets.Asset
import assets.Asset.ingame.ui.*
import assets.Asset.ingame.abilities.*
import assets.Asset.ingame.entities.*
import assets.Asset.ingame.ui.bars.minimalistBar
import com.raquo.laminar.api.A.*
import game.ui.gui.reactivecomponents.*
import game.ui.reactivepixi.AttributeModifierBuilder.*
import game.ui.reactivepixi.ChildrenReceiver.*
import game.ui.reactivepixi.ReactivePixiElement.*
import game.ui.reactivepixi.ReactiveStage
import gamelogic.abilities.Ability
import gamelogic.entities.{Entity, LivingEntity, MovingBody}
import gamelogic.gamestate.GameState
import be.doeraene.physics.Complex
import be.doeraene.physics.Complex.i
import typings.pixiJs.PIXI.{LoaderResource, RenderTexture}
import typings.pixiJs.mod.Graphics
import utils.misc.RGBColour
import typings.pixiJs.PIXI.SCALE_MODES
import game.ui.gui.reactivecomponents.*
import game.ui.gui.reactivecomponents.gridcontainer.GridContainer
import gamecommunication.ServerToClient.BeginIn

final class ReactiveGUIDrawer(
    playerId: Entity.Id,
    stage: ReactiveStage,
    resources: PartialFunction[Asset, LoaderResource],
    useAbilityWriter: Observer[Ability.AbilityId],
    gameStateUpdates: EventStream[(GameState, Long)],
    beginInEvents: EventStream[BeginIn]
) {
  val linearMode = 1.0

  private val blackTexture = {
    val graphics = new Graphics

    graphics
      .lineStyle(2, 0)
      .beginFill(0, 0)
      .drawRect(0, 0, 32, 32)
      .endFill()

    stage.application.renderer.generateTexture(graphics, linearMode, 1)
  }

  val slowGameStateUpdates: EventStream[(GameState, Long)] = gameStateUpdates.throttle(500)

  val guiContainer: ReactiveContainer = pixiContainer()
  stage(guiContainer)

  guiContainer.amend(new ClockDisplay(slowGameStateUpdates, Val(10 + 10 * i)))
  guiContainer.amend(
    new FPSDisplay(gameStateUpdates).amend(
      y <-- stage.resizeEvents.map(_._2 - 50)
    )
  )
  guiContainer.amend(
    new BeginCountdownDisplay(
      beginInEvents,
      gameStateUpdates.map(_._2),
      stage.resizeEvents.map((width, height) => Complex(width / 2, height / 2 - 100))
    )
  )

  val abilityButtonContainer: ReactiveContainer = new GridContainer(
    GridContainer.Column,
    gameStateUpdates
      .map(_._1.players.get(playerId))
      .collect { case Some(player) => player.allowedAbilities.distinct }
      .toSignal(List.empty)
      .map { abilities =>
        abilities.map { abilityId =>
          new AbilityButton(
            abilityId,
            playerId,
            useAbilityWriter,
            resources(Asset.abilityAssetMap(abilityId)).texture,
            resources(assets.Asset.ingame.abilities.abilityOverlay).texture,
            gameStateUpdates,
            Val((32, 32))
          ): ReactiveContainer
        }
      },
    10
  ).amend(
    y <-- stage.resizeEvents.map(_._2 - 32),
    x <-- stage.resizeEvents.map(_._1 / 2)
  )
  guiContainer.amend(abilityButtonContainer)

  val playerGridSize = Val(120 + 30 * i)
  val playerFrameGridContainer: ReactiveContainer = new GridContainer(
    GridContainer.Row,
    gameStateUpdates
      .map(_._1.players.valuesIterator.map(_.id).toList.sorted)
      .toSignal(Nil)
      .split(identity) { case (entityId, _, _) =>
        new PlayerFrame(
          entityId,
          resources(minimalistBar).texture,
          resources(minimalistBar).texture,
          playerGridSize.map(_.tuple),
          resources,
          gameStateUpdates
        ): ReactiveContainer
      },
    10
  ).amend(
    x <-- stage.resizeEvents.map(_._1).combineWith(playerGridSize.map(_.re)).map(_ - _)
  )
  guiContainer.amend(playerFrameGridContainer)

}
