package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{Polygon, Shape}
import gamelogic.entities.{Body, Entity}
import entitiescollections.CaptureTheFlagInfo

/** A Zone is an area in the game that can be used to check if something is in the zone.
  *
  * A Zone is not part of the game state, and does not have any kind of interpretation like DamageZone or HealUnit. We
  * always give the id 0 to the Zone. It is just a Body for convenience methods.
  */
final case class Zone(xPos: Double, yPos: Double, shape: Shape, rotation: Double = 0.0) extends Body {

  val id: Entity.Id = Entity.Id.initial

  val pos: Complex = Complex(xPos, yPos)

  def time: Long = 0L

}

object Zone {

  def flagZones(teamNumbers: Seq[Int]): Seq[Zone] = teamNumbers.length match {
    case 2 =>
      val gWidth = CaptureTheFlagInfo.dimensions(2)._1
      val shape = Polygon(
        Vector(
          Complex(-40, -30),
          Complex(60, -30),
          Complex(60, 30),
          Complex(-40, 30)
        ),
        convex = true
      )
      List(
        new Zone(-gWidth / 2 + 40, 0, shape),
        new Zone(gWidth / 2 - 40, 0, shape)
      )
    case _ =>
      throw new NotImplementedError("Flag zones are only implemented for two teams.")
  }

}
