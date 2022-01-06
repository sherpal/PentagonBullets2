package gamelogic.entities.concreteentities

import be.doeraene.physics.shape.Circle
import gamelogic.entities.{Entity, MovingBody}

/** The trait BulletLike gathers common feature of Entities that act like Bullets.
  *
  * The current two concrete classes are
  *   - Bullet
  *   - SmashBullet
  *
  * For a game called Pentagon Bullets, it seems natural to have a special trait for bullets!
  */
trait BulletLike extends MovingBody {

  val time: Long

  val ownerId: Entity.Id

  val radius: Int

  val shape: Circle = Circle(radius)

  val moving: Boolean = true

  val rotation: Double = 0

  val travelledDistance: Double

  def currentTravelledDistance(now: Long): Double = travelledDistance + currentPosition(now).distanceTo(pos)

}
