package game.ui.effects

import game.Camera
import gamelogic.gamestate.GameState
import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Shape
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.{Container, Sprite}

final class ShapeEffect(
    shape: Shape,
    position: Complex,
    rotation: Double,
    endingCondition: (Long, GameState) => Boolean,
    camera: Camera,
    texture: Texture
) extends GameEffect {

  val sprite = new Sprite(texture)
  sprite.rotation = -rotation
  sprite.anchor.set(0, 0)

  override def addToContainer(container: Container): Unit = container.addChild(sprite)

  def destroy(): Unit =
    sprite.destroy()

  override def isOver(currentTime: Long, gameState: GameState): Boolean = endingCondition(currentTime, gameState)

  def update(currentTime: Long, gameState: GameState): Unit =
    if isOver(currentTime, gameState) then sprite.visible = false
    else camera.viewportManager(sprite, position, shape.boundingBox)

}
