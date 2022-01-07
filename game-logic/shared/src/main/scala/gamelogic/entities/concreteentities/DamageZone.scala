package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Circle
import gamelogic.entities.{Body, Entity}

/** A DamageZone hurts player that stands in it.
  *
  * lastTick is the time at which the DamageZone hurt all the players within.
  */
final case class DamageZone(
    id: Entity.Id,
    lastGrow: Long,
    lastTick: Long,
    pos: Complex,
    shape: Circle
) extends Body {

  def time: Long = lastGrow

  val rotation: Double = 0.0

  def radius: Int = shape.radius.toInt

}

object DamageZone {

  // A DamageZone deals one damage every half second.
  val tickRate: Long       = 500
  val damageOnTick: Double = 5

  // A DamageZone starts at radius 5, and its radius increase by 2 every 2 seconds up to 150.
  val startingRadius: Int = 5
  val growingRate: Long   = 2000
  val growingValue: Int   = 2
  val maxRadius: Int      = 150

  // A DamageZone pops every ~ seconds
  val popRate: Long = 7000

}
