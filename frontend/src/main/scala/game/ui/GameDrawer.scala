package game.ui

import assets.Asset
import be.doeraene.physics.Complex
import com.raquo.laminar.api.A.*
import game.Camera
import game.ui.reactivepixi.AttributeModifierBuilder.*
import game.ui.reactivepixi.ChildrenReceiver.children
import game.ui.reactivepixi.ReactivePixiElement.*
import game.ui.reactivepixi.ReactiveStage
import gamelogic.abilities.Ability
import gamelogic.entities
import gamelogic.gamestate.GameState
import gamelogic.entities.{concreteentities, Entity}
import gamelogic.entities.concreteentities.*
import typings.pixiJs.PIXI.{DisplayObject, LoaderResource, Texture}
import typings.pixiJs.mod.{DisplayObject as _, *}

import scala.collection.mutable

/** This class is used to draw the game state at any moment in time. The implementation is full of side effects,
  * probably in the wrong fear of less good performance.
  */
final class GameDrawer(
    reactiveStage: ReactiveStage,
    resources: PartialFunction[Asset, LoaderResource]
)(implicit owner: Owner)
    extends Drawer
    with AbilityGiverDrawer
    with BarrierDrawer
    with BuffsDrawer
    with BulletDrawer
    with DamageZoneDrawer
    with GunTurretDrawer
    with HealUnitDrawer
    with HealingZoneDrawer
    with LaserLauncherDrawer
    with MistDrawer
    with ObstacleDrawer
    with PlayerDrawer {

  @inline def application: Application = reactiveStage.application
  @inline def camera: Camera           = reactiveStage.camera

  def abilityImagesTextures(abilityId: Ability.AbilityId): Texture = resources(
    assets.Asset.abilityAssetMap(abilityId)
  ).texture

  def healingZoneTexture: Texture = resources(assets.Asset.ingame.entities.healingZone).texture

  val otherStuffContainerBelow: ReactiveContainer = pixiContainer()
  val mistContainer: ReactiveContainer            = pixiContainer()
  val damageZoneContainer: ReactiveContainer      = pixiContainer()
  val bulletContainer: ReactiveContainer          = pixiContainer()
  val playerContainer: ReactiveContainer          = pixiContainer()
  val movingStuffContainer: ReactiveContainer     = pixiContainer()
  val obstacleContainer: ReactiveContainer        = pixiContainer()
  val laserLauncherContainer: ReactiveContainer   = pixiContainer()
  val healingZoneContainer: ReactiveContainer     = pixiContainer()
  val gunTurretContainer: ReactiveContainer       = pixiContainer()
  val barrierContainer: ReactiveContainer         = pixiContainer()
  val healUnitContainer: ReactiveContainer        = pixiContainer()
  val abilityGiverContainer: ReactiveContainer    = pixiContainer()
  val otherStuffContainerAbove: ReactiveContainer = pixiContainer()

  reactiveStage(
    otherStuffContainerBelow,
    mistContainer,
    damageZoneContainer,
    bulletContainer,
    playerContainer,
    movingStuffContainer,
    laserLauncherContainer,
    obstacleContainer,
    healingZoneContainer,
    barrierContainer,
    gunTurretContainer,
    healUnitContainer,
    abilityGiverContainer,
    otherStuffContainerAbove
  )

  def maybeEntityDisplayObjectById(entityId: Entity.Id): Option[DisplayObject] =
    players
      .get(entityId)
      .map(_._1)

  def drawGameState(gameState: GameState, cameraPosition: Complex, currentTime: Long): Unit = {
    camera.worldCenter = cameraPosition

    val playerColours = (gameState.players ++ gameState.deadPlayers).map((id, player) => (id, player.colour))

    val teamColours = (gameState.players ++ gameState.deadPlayers).values
      .groupBy(_.team)
      .map((teamId, players) => teamId -> players.minBy(_.id).colour)

    drawAbilityGivers(gameState.abilityGivers)
    drawBarriers(gameState, playerColours)
    drawPlayerBuffs(gameState, currentTime)
    drawBullets(gameState, currentTime, playerColours)
    drawDamageZones(gameState.damageZones)
    drawGunTurrets(gameState, playerColours)
    drawHealingZones(gameState, playerColours)
    drawHealUnits(gameState.healUnits)
    drawLaserLauncher(gameState, playerColours)
    drawMists(gameState.mists)
    drawObstacles(gameState.obstacles.values.toList)
    drawPlayers(gameState, currentTime, teamColours)

  }

}
