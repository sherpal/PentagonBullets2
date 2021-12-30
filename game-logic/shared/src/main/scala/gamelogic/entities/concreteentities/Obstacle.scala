package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{Polygon, Shape}
import gamelogic.entities.{Body, Entity}

/** An Obstacle is something in the Game Area that people and bullets can't cross.
  */
final case class Obstacle(id: Entity.Id, xPos: Double, yPos: Double, shape: Shape) extends Body {

  val rotation: Double = 0.0

  val pos: Complex = Complex(xPos, yPos)

  def time: Long = 0L

}

object Obstacle {

  def apply(id: Entity.Id, pos: Complex, vertices: Vector[Complex]): Obstacle = new Obstacle(
    id,
    pos.re,
    pos.im,
    Polygon(vertices)
  )

  def apply(id: Entity.Id, pos: Complex, z1: Complex, z2: Complex, thickness: Double): Obstacle = new Obstacle(
    id,
    pos.re,
    pos.im,
    Polygon(segmentObstacleVertices(z1, z2, thickness), convex = true)
  )

  def segmentObstacleVertices(z1: Complex, z2: Complex, thickness: Double): Vector[Complex] = {
    val diff = z2 - z1

    if (diff.modulus2 < 1) {
      throw new RuntimeException("Obstacle is too small")
    }

    val orthogonal     = Complex(diff.im, -diff.re)
    val orthogonalNorm = orthogonal / orthogonal.modulus

    val p1 = z1 + thickness / 2 * orthogonalNorm
    val p4 = z1 - thickness / 2 * orthogonalNorm

    val p2 = z2 + thickness / 2 * orthogonalNorm
    val p3 = z2 - thickness / 2 * orthogonalNorm

    Vector(p1, p2, p3, p4)
  }

}
