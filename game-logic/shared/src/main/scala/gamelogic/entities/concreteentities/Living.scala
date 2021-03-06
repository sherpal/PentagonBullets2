package gamelogic.entities.concreteentities

import gamelogic.entities.Entity

/** A Living Entity has life, can take damage or heal, and can be dead.
  */
trait Living extends Entity {
  val lifeTotal: Double

  def maxLife: Double
}
