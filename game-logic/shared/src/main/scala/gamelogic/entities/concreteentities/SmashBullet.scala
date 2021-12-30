package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import gamelogic.entities.{Entity, MovingBody}
import gamelogic.entities.WithPosition.Angle

final case class SmashBullet(
    id: Entity.Id,
    time: Long,
    ownerId: Entity.Id,
    xPos: Double,
    yPos: Double,
    radius: Int,
    direction: Double,
    speed: Double,
    travelledDistance: Double,
    lastGrow: Long
) extends BulletLike {

  def pos: Complex = Complex(xPos, yPos)

  def changeRadius(time: Long, newRadius: Int): SmashBullet = copy(radius = newRadius, lastGrow = time)

  def move(
      time: Long,
      position: Complex,
      direction: Angle,
      rotation: Angle,
      speed: Double,
      moving: Boolean
  ): MovingBody = copy(
    time = time,
    xPos = position.re,
    yPos = position.im,
    direction = direction,
    speed = speed
  )

}

object SmashBullet {

  val defaultRadius: Int = 5 * Bullet.defaultRadius

  val endRadius: Int = 3 * defaultRadius

  val reach: Int = Bullet.reach / 2

  val speed: Double = Bullet.speed

  val lifeTime: Long = math.round(1000 * reach / speed)

  val growRate: Long = lifeTime / 4

  val growValue: Int = (endRadius - defaultRadius) / 4

}
