package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import gamelogic.entities.{Entity, MovingBody}
import gamelogic.entities.WithPosition.Angle

/** A Player represents a Human player.
  */
final case class Bullet(
    id: Entity.Id,
    time: Long,
    ownerId: Entity.Id,
    teamId: Int,
    xPos: Double,
    yPos: Double,
    radius: Int,
    direction: Double,
    speed: Double,
    travelledDistance: Double
) extends BulletLike {

  def pos: Complex = Complex(xPos, yPos)

  def withRadius(newRadius: Int): Bullet = copy(radius = newRadius)

  def move(time: Long, position: Complex, direction: Angle, rotation: Angle, speed: Double, moving: Boolean): Bullet =
    copy(
      time = time,
      xPos = position.re,
      yPos = position.im,
      direction = direction,
      speed = speed
    )

}

object Bullet {
  val defaultRadius: Int = 4

  val speed: Int = 400

  val reach: Int = 1000

  val damage: Double = 10

  val reloadTime: Long = 1000 / 11

  def damageFromRadius(radius: Int): Double = radius * 10 / 4.0
}
