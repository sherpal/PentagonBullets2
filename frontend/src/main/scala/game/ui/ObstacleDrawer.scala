package game.ui

import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.Obstacle
import typings.pixiJs.mod.Sprite

import scala.collection.mutable

trait ObstacleDrawer extends Drawer {

  def camera: Camera

  def obstacleContainer: ReactiveContainer

  private val obstacleSprites: mutable.Map[Entity.Id, Sprite] = mutable.Map.empty
  def drawObstacles(obstacles: List[Obstacle]): Unit = obstacles.foreach { obstacle =>
    val sprite = obstacleSprites.getOrElse(
      obstacle.id, {
        val s = new Sprite(polygonTexture(obstacle.colour.intColour, 1.0, obstacle.shape))
        s.anchor.set(0.5, 0.5)
        obstacleSprites += (obstacle.id -> s)
        obstacleContainer.ref.addChild(s)
        s
      }
    )

    camera.viewportManager(sprite, obstacle.pos, obstacle.shape.boundingBox)
  }

}
