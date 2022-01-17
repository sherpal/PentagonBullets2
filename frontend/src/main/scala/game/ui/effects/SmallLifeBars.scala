package game.ui.effects

import game.Camera
import game.ui.effects.GameEffect
import gamelogic.entities.{Body, Entity, LivingEntity, WithAbilities}
import gamelogic.gamestate.GameState
import be.doeraene.physics.Complex
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.{Container, Graphics, Sprite}
import utils.misc.RGBColour

import scala.util.Random

final class SmallLifeBars(
    entityId: Entity.Id,
    startTime: Long,
    backgroundTexture: Texture,
    barTexture: Texture,
    camera: Camera
) extends GameEffect {

  import Complex._

  private val width  = 40.0
  private val height = 7.0

  private val red    = RGBColour.red.intColour
  private val orange = RGBColour.orange.intColour
  private val green  = RGBColour.green.intColour

  private val barContainer: Container = new Container
  private val background              = new Sprite(backgroundTexture)
  private val bar                     = new Sprite(barTexture)
  private val mask                    = new Graphics
  bar.mask = mask
  barContainer.addChild(background)
  background.width = width
  background.height = height
  barContainer.addChild(bar)
  bar.width = width
  bar.height = height
  barContainer.addChild(mask)

  /** Returns a value between 0 and 1, depending on the life percentage amount of the hound. */
  def computeValue(living: LivingEntity): Double =
    living.lifeTotal / living.maxLife

  private val resourceBackground = new Sprite(backgroundTexture)
  private val resourceBar        = new Sprite(barTexture)
  private val resourceMask       = new Graphics
  resourceBar.mask = resourceMask
  //barContainer.addChild(resourceBackground)
  resourceBackground.width = width
  resourceBackground.height = 3.0
  resourceBackground.y = height
  resourceBackground.tint = 0
  barContainer.addChild(resourceBar)
  resourceBar.width = width
  resourceBar.height = 3.0
  resourceBar.y = height
  barContainer.addChild(resourceMask)

  def computeResourceValue(withResource: WithAbilities): Double =
    withResource.resourceAmount.amount / withResource.maxResourceAmount

  def destroy(): Unit = barContainer.destroy()

  def update(currentTime: Long, gameState: GameState): Unit =
    gameState.entityByIdAs[Body & LivingEntity](entityId).fold[Unit](barContainer.visible = false) { entity =>
      if (!barContainer.visible) barContainer.visible = true

      val lifePercentage = computeValue(entity)

      bar.tint = lifePercentage match {
        case x if x <= 0.2 => red
        case x if x <= 0.5 => orange
        case _             => green
      }

      mask
        .clear()
        .beginFill(0xc0c0c0)
        .drawRect(0, 0, bar.width * lifePercentage, bar.height)

      Some(entity).collect { case withAbilities: WithAbilities => withAbilities }.foreach {
        (entityWithAbilities: WithAbilities) =>
          val resourcePercentage = computeResourceValue(entityWithAbilities)
          resourceBar.tint = entityWithAbilities.resourceType.colour.intColour

          resourceMask
            .clear()
            .beginFill(0xc0c0c0)
            .drawRect(0, resourceBar.y, resourceBar.width * resourcePercentage, resourceBar.height)
      }

      val entityPosition = entity.currentPosition(currentTime)
      val verticalOffset = (entity.shape.radius + height).i
      camera.viewportManager(
        barContainer,
        entityPosition - width / 2 - verticalOffset - height.i / 2,
        entityPosition + verticalOffset,
        entity.shape.boundingBox
      )
    }

  def isOver(currentTime: Long, gameState: GameState): Boolean =
    currentTime > startTime && !gameState.entities.contains(entityId)

  def addToContainer(container: Container): Unit = container.addChild(barContainer)
}
