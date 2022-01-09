package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Circle
import gamelogic.entities.*

final case class GunTurret(
    creationTime: Long,
    id: Entity.Id,
    ownerId: Entity.Id,
    teamId: Int,
    xPos: Double,
    yPos: Double,
    lastShot: Long,
    radius: Double,
    rotation: Double,
    lifeTotal: Double
) extends Body
    with LivingEntity {

  def takesDamage(amount: Double): GunTurret = copy(lifeTotal = lifeTotal - amount)

  def shootTowards(rotation: Double, time: Long): GunTurret =
    copy(rotation = rotation, lastShot = time)

  def pos: Complex = Complex(xPos, yPos)

  def time: Long = creationTime

  val shape: Circle = Circle(radius)

  def maxLife: Double = GunTurret.maxLifeTotal

  protected def patchLifeTotal(newLife: Double): GunTurret = copy(lifeTotal = newLife)

}

object GunTurret {

  val shootRate: Long = 200 // shoot every 200 ms.

  val defaultRadius: Double = 15

  val maxLifeTotal: Double = 100

  val defaultReach: Int = 700

}
