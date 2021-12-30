package gamelogic.entities

import be.doeraene.physics.Complex

/** A WithPosition Entity has a `pos` value that determines their place in the world.
  */
trait WithPosition extends Entity {
  def pos: Complex

  def currentPosition(time: Long): Complex = pos
}

object WithPosition {

  type Angle = Double

}
