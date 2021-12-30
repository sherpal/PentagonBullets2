package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{Circle, Shape}
import gamelogic.entities.{Body, Entity}

/** A HealUnit pops at random places during the Game and heal players for some small amount.
  */
final case class HealUnit(id: Entity.Id, time: Long, xPos: Double, yPos: Double) extends Body {

  def pos: Complex = Complex(xPos, yPos)

  val shape: Shape = Circle(HealUnit.radius)

  val rotation: Double = 0.0

}

object HealUnit {
  val radius: Double = 10

  val lifeTime: Long = 15000 // 15 seconds before disappearing

  val lifeGain: Double = 15 // restore 15 life points when a player takes it.

  def playerTakeUnit(player: Player, time: Long): Player =
    player.copy(lifeTotal = math.min(player.lifeTotal + lifeGain, Player.maxLife))
}
