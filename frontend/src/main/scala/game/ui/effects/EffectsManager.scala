package game.ui.effects

import assets.Asset
import com.raquo.laminar.api.A.*
import game.Camera
import game.ui.Drawer
import gamelogic.abilities
import gamelogic.entities.{Body, Entity}
import gamelogic.gamestate.gameactions.*
import gamelogic.gamestate.{GameAction, GameState}
import be.doeraene.physics.Complex
import be.doeraene.physics.shape.*
import game.ui.effects.abilities.LaserLauncherLink
import gamelogic.abilities.LaserAbility
import gamelogic.entities.concreteentities.HealUnit
import typings.pixiJs.PIXI.{LoaderResource, RenderTexture}
import typings.pixiJs.mod.{Application, Container, Graphics}
import typings.pixiJs.PIXI.DisplayObject
import utils.misc.RGBColour
import typings.pixiJs.PIXI.SCALE_MODES

import scala.collection.mutable

final class EffectsManager(
    playerId: Entity.Id,
    $actionsAndStates: EventStream[(GameAction, GameState)],
    camera: Camera,
    val application: Application,
    resources: PartialFunction[Asset, LoaderResource]
)(implicit owner: Owner)
    extends Drawer {

  import Complex.DoubleWithI

  val triangleHitTexture: RenderTexture = {
    val graphics = new Graphics()
      .beginFill(0)
      .drawRect(0, 0, 20, 2)
      .endFill()

    application.renderer.generateTexture(graphics, linearScale, 1)
  }

  private val container: Container = new Container
  application.stage.addChild(container)

  private val gameEffects: mutable.Set[GameEffect] = mutable.Set.empty

  $actionsAndStates.foreach { case (action, gameState) =>
    gameState
      .applyActionChangers(action)
      .flatMap {
        case PlayerTakeDamage(_, time, entityId, _, amount, _) if entityId == playerId =>
          Some(
            new SimpleTextEffect(
              amount.toInt.toString,
              RGBColour.red,
              time,
              Path.goDown(2000, 40).jitter(math.Pi / 16) + gameState.players.get(playerId).fold(Complex.zero)(_.pos),
              camera
            )
          )
        case PlayerHitByMultipleBullets(actionId, time, bulletIds, entityId, totalDamage, actionSource)
            if playerId == entityId =>
          Some(
            new SimpleTextEffect(
              totalDamage.toInt.toString,
              RGBColour.red,
              time,
              Path.goDown(2000, 40).jitter(math.Pi / 16) + gameState.players.get(playerId).fold(Complex.zero)(_.pos),
              camera
            )
          )
        case PlayerTakeHealUnit(_, time, entityId, _, _) if entityId == playerId =>
          Some(
            new SimpleTextEffect(
              HealUnit.lifeGain.toInt.toString,
              RGBColour.green,
              time,
              Path
                .goUp(2000, 40)
                .jitter(math.Pi / 16) + gameState.entityByIdAs[Body](entityId).fold(Complex.zero)(_.pos),
              camera
            )
          )
        case UseAbilityAction(_, time, ability: LaserAbility, _, _)
            if ability.casterId == playerId && ability.stepNumber == 0 =>
          Some(new LaserLauncherLink(ability.casterId, camera))
        case action: FireLaser =>
          gameState
            .playerById(action.ownerId)
            .map { caster =>
              val shape = action.shape.translateBy(-caster.pos)
              FlashingShape(
                shape,
                caster.pos + shape.vertices.minBy(_.re).realProjection + shape.vertices.maxBy(_.im).imagProjection,
                0,
                action.time,
                750L,
                camera,
                polygonTexture(caster.colour.intColour, 0.9, shape)
              )
            }
        case NewGunTurret(actionId, time, turretId, ownerId, teamId, pos, radius, actionSource) =>
          Some(
            new SmallLifeBars(
              turretId,
              time,
              resources(Asset.ingame.ui.bars.minimalistBar).texture,
              resources(Asset.ingame.ui.bars.minimalistBar).texture,
              camera
            )
          )
        case NewPlayer(actionId, player, time, actionSource) =>
          Some(
            new SmallLifeBars(
              player.id,
              time,
              resources(Asset.ingame.ui.bars.minimalistBar).texture,
              resources(Asset.ingame.ui.bars.minimalistBar).texture,
              camera
            )
          )
        case _ =>
          Option.empty[SimpleTextEffect]
      }
      .foreach { effect =>
        effect.addToContainer(container)
        gameEffects += effect
      }

  }

  def update(currentTime: Long, gameState: GameState): Unit =
    gameEffects.foreach { effect =>
      if (effect.isOver(currentTime, gameState)) {
        gameEffects -= effect
        effect.destroy()
      } else {
        effect.update(currentTime, gameState)
      }
    }

  def maybeEntityDisplayObjectById(entityId: Entity.Id): Option[DisplayObject] = None

}
