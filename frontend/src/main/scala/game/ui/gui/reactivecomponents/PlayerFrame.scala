package game.ui.gui.reactivecomponents

import assets.Asset
import com.raquo.laminar.api.A.*
import gamelogic.entities.Entity
import game.ui.reactivepixi.AttributeModifierBuilder.*
import game.ui.reactivepixi.EventModifierBuilder.*
import game.ui.reactivepixi.PixiModifier
import game.ui.reactivepixi.ReactivePixiElement.*
import typings.pixiJs.PIXI.{Graphics, LoaderResource, Texture}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.GameState
import typings.pixiJs.anon.Align
import typings.pixiJs.mod.{Rectangle, TextStyle}
import utils.misc.RGBColour
import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Polygon
import models.syntax.Pointed

//noinspection TypeAnnotation
final class PlayerFrame(
    entityId: Entity.Id,
    lifeTexture: Texture,
    resourceTexture: Texture,
    dimensions: Signal[(Double, Double)], // signal for width and height
    resources: PartialFunction[Asset, LoaderResource],
    gameStateUpdates: EventStream[(GameState, Long)]
) extends GUIComponent {

  val playerBulletShape = resources(Asset.ingame.ui.playerItemBullet).texture

  val onlyGameStates = gameStateUpdates.map(_._1)

  // Calling deadPlayers is safe because a player is either alive or dead.
  val entityEvents = onlyGameStates
    .map(gameState =>
      try gameState.entityByIdAs[Player](entityId).getOrElse(gameState.deadPlayers(entityId))
      catch {
        case e: Throwable =>
          e.printStackTrace()
          println(gameState.players)
          println(gameState.deadPlayers)
          implicit def entityIdPointed: Pointed[Entity.Id] = Pointed.factory(entityId)
          implicit def polygonPointed: Pointed[Polygon]    = Pointed.factory(Player.shape)
          Pointed[Player].unit.copy(name = "aaaaaaaaarh")
      }
    )

  val entityIsAliveEvents = onlyGameStates.map(_.isPlayerAlive(entityId))

  val heightSignal: Signal[Double]    = dimensions.map(_._2)
  val barsWidthSignal: Signal[Double] = dimensions.map { case (w, h) => w - h }

  val entityWithDimensionsEvents: EventStream[(Player, (Double, Double))] =
    entityEvents
      .withCurrentValueOf(barsWidthSignal.combineWith(heightSignal))
      .map((player, width, height) => (player, (width, height)))

  val lifeProportion                       = 0.8
  val lifeSpriteHeight: Signal[Double]     = heightSignal.map(_ * lifeProportion)
  val resourceSpriteHeight: Signal[Double] = heightSignal.map(_ * (1 - lifeProportion))

  private def adaptMask(width: Double, height: Double, ratio: Double): Graphics => Unit =
    _.clear().beginFill(0x000000).drawRect(0, 0, width * ratio, height)

  val lifeMask: ReactiveGraphics = pixiGraphics(
    x <-- heightSignal,
    moveGraphics <-- entityWithDimensionsEvents.map { case (entity, (width, height)) =>
      adaptMask(width, height, (entity.lifeTotal max 0.0) / entity.maxLife)
    }
  )
  val shapeSprite: ReactiveSprite = pixiSprite(
    playerBulletShape,
    dims    <-- heightSignal.map(h => (h, h)),
    tintInt <-- entityEvents.map(_.colour.intColour).toSignal(0)
  )

  val backgroundLifeSprite: ReactiveSprite = pixiSprite(
    lifeTexture,
    tint := RGBColour.gray,
    x      <-- heightSignal,
    width  <-- barsWidthSignal,
    height <-- lifeSpriteHeight
  )
  val lifeSprite: ReactiveSprite = pixiSprite(
    lifeTexture,
    mask := lifeMask,
    tint := RGBColour.green,
    x      <-- heightSignal,
    width  <-- barsWidthSignal,
    height <-- lifeSpriteHeight
  )
  val resourceMask: ReactiveGraphics = pixiGraphics(
    x <-- heightSignal,
    y <-- lifeSpriteHeight,
    moveGraphics <-- entityWithDimensionsEvents.map { case (entity, (width, height)) =>
      adaptMask(width, height, entity.resourceAmount.amount / entity.maxResourceAmount)
    }
  )
  val resourceSprite: ReactiveSprite = pixiSprite(
    resourceTexture,
    mask := resourceMask,
    tint   <-- entityEvents.map(_.resourceType.colour).toSignal(RGBColour.white),
    x      <-- heightSignal,
    y      <-- lifeSpriteHeight,
    width  <-- barsWidthSignal,
    height <-- resourceSpriteHeight
  )

  val playerNameText: ReactiveText = pixiText(
    "",
    text <-- entityEvents.map(_.name).toSignal(""),
    textStyle := new TextStyle(Align().setFontSize(10.0)),
    x <-- heightSignal.map(_ + 4),
    y := 2,
    tint := RGBColour.white
  )

  val lifeText: ReactiveText = pixiText(
    "",
    text <-- entityEvents.map(_.lifeTotal.toInt.max(0).toString).toSignal(""),
    x    <-- dimensions.map(_._1 - 30),
    textStyle := new TextStyle(Align().setFontSize(15.0))
  )

  container.amend(
    shapeSprite,
    backgroundLifeSprite,
    lifeSprite,
    lifeMask,
    resourceSprite,
    resourceMask,
    playerNameText,
    lifeText,
    interactive := true,
    hitArea <-- dimensions.map { case (width, height) => new Rectangle(0, 0, width, height) },
    visible <-- Val(true)
  )

}
