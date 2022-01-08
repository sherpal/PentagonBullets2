package game.ui.effects

import game.Camera
import gamelogic.gamestate.GameState
import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Shape
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.{Container, Sprite}

object FlashingShape {
  def apply(
      shape: Shape,
      position: Complex,
      rotation: Double,
      startTime: Long,
      duration: Long,
      camera: Camera,
      texture: Texture
  ): GameEffect = new ShapeEffect(
    shape,
    position,
    rotation,
    (time, _) => time - startTime > duration,
    camera,
    texture
  )
}
