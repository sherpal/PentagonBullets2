package gamelogic.entities

import io.circe.{Decoder, Encoder}
import utils.misc.RGBColour

sealed trait Resource {
  def colour: RGBColour
}

object Resource {

  case class ResourceAmount(amount: Double, resourceType: Resource) extends PartiallyOrdered[ResourceAmount] {
    def unary_- : ResourceAmount = copy(amount = -amount)

    def +[R1 <: Resource](that: ResourceAmount): ResourceAmount =
      if (this.resourceType == that.resourceType) ResourceAmount(this.amount + that.amount, resourceType)
      else this

    def -[R1 <: Resource](that: ResourceAmount): ResourceAmount =
      if (this.resourceType == that.resourceType) ResourceAmount(this.amount - that.amount, resourceType)
      else this

    def max(x: Double): ResourceAmount = ResourceAmount(x max amount, resourceType)
    def min(x: Double): ResourceAmount = ResourceAmount(x min amount, resourceType)

    /** Returns a [[ResourceAmount]] whose amount is between 0 and `maxValue`.
      */
    def clampTo(maxValue: Double): ResourceAmount = ResourceAmount((amount max 0) min maxValue, resourceType)

    /** Partial ordering of the [[ResourceAmount]]
      *
      * The rule is that: if both [[ResourceAmount]] have the same [[Resource]], they are compared according to their
      * amounts if exactly one of the [[ResourceAmount]]s is a [[NoResource]], it is smaller otherwise, they can't be
      * compared
      */
    def tryCompareTo[B >: ResourceAmount](that: B)(implicit evidence$1: AsPartiallyOrdered[B]): Option[Int] =
      that match {
        case that: ResourceAmount if that.resourceType == this.resourceType => Some(this.amount compare that.amount)
        case that: ResourceAmount if that.resourceType != NoResource && this.resourceType == NoResource => Some(-1)
        case that: ResourceAmount if that.resourceType == NoResource && this.resourceType != NoResource => Some(1)
        case _                                                                                          => None
      }
  }

  case object Mana extends Resource {
    def colour: RGBColour = RGBColour.fromIntColour(0x0000ff)
  }
  case object Energy extends Resource {
    def colour: RGBColour = RGBColour.fromIntColour(0xffff00)
  }
  case object Rage extends Resource {
    def colour: RGBColour = RGBColour.fromIntColour(0xff0000)
  }

  case object NoResource extends Resource {
    def colour: RGBColour = RGBColour.fromIntColour(0)
  }

  val noResourceAmount: ResourceAmount = ResourceAmount(0.0, NoResource)
  val zero: ResourceAmount             = noResourceAmount

  final val resources: Map[String, Resource] = Map(
    Mana.toString       -> Mana,
    Energy.toString     -> Energy,
    NoResource.toString -> NoResource
  )
  private def fromString(resourceStr: String): Resource = resources(resourceStr)

  implicit final val resourceDecoder: Decoder[Resource] = Decoder.decodeString.map(fromString)
  implicit final val resourceEncoder: Encoder[Resource] = Encoder.encodeString.contramap(_.toString)

}
