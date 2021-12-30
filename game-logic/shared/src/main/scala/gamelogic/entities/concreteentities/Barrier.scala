package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{Polygon, Shape}
import gamelogic.entities._

/** A Barrier is like an Obstacle attached to the player, so that its owner and their bullets can go through, but not
  * other entities.
  */
final case class Barrier(
    id: Entity.Id,
    time: Long,
    ownerId: Entity.Id,
    teamId: Int,
    xPos: Double,
    yPos: Double,
    rotation: Double,
    shape: Shape
) extends Body {
  def pos: Complex = Complex(xPos, yPos)
}

object Barrier {

  val lifeTime: Long = 5000

  val length: Double = 75
  val width: Double  = 75

  private val vertices: Vector[Complex] = Vector(
    Complex(width / 2, -length / 2),
    Complex(width / 2, length / 2),
    Complex(-width / 2, length / 2),
    Complex(-width / 2, -length / 2)
  )

  val shape: Polygon = Polygon(vertices, convex = true)

}
