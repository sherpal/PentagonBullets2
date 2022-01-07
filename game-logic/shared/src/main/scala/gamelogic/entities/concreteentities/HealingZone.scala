package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Circle
import gamelogic.entities._

/** A HealingZone is attached to a Team, and heal any member of the Team that stands in it.
  *
  * A HealingZone has a maximum amount of life that it can give.
  */
final case class HealingZone(
    id: Entity.Id,
    creationTime: Long,
    ownerId: Entity.Id,
    lastTick: Long,
    lifeSupply: Double,
    pos: Complex,
    shape: Circle
) extends Body {

  def subtractLifeSupply(amount: Double): HealingZone = copy(lifeSupply = lifeSupply - amount)

  def time: Long = creationTime

  val rotation: Double = 0.0

  def radius: Double = shape.radius

  val ticksRemaining: Int = lifeSupply.toInt / HealingZone.healingOnTick

}

object HealingZone {

  val radius: Double = 40

  val lifeSupply: Int    = 40
  val healingOnTick: Int = 5

  val tickRate: Long = 500

  val lifetime: Long = 60000

}
