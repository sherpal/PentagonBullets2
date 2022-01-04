package utils.misc

import models.syntax.Pointed

import scala.language.implicitConversions
import scala.util.Random

final case class RGBColour(red: Int, green: Int, blue: Int) extends Colour {
  def alpha: Double                        = 1
  def withAlpha(alpha: Double): RGBAColour = RGBAColour(red, green, blue, alpha)
  def withoutAlpha: RGBColour              = this
  def asRGBAColour: RGBAColour             = withAlpha(1.0)
}

object RGBColour {
  def fromIntColour(colour: Int): RGBColour = RGBColour(
    colour >> 16,
    (colour % (256 << 8)) / 256,
    colour % 256
  )
  import io.circe._
  import io.circe.generic.semiauto._
  implicit val fooDecoder: Decoder[RGBColour] = deriveDecoder[RGBColour]
  implicit val fooEncoder: Encoder[RGBColour] = deriveEncoder[RGBColour]

  implicit def pointed: Pointed[RGBColour] = Pointed.factory(
    RGBColour(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))
  )

  implicit def asRGBA(rgb: RGBColour): RGBAColour = rgb.withAlpha(1.0)

  val black: RGBColour        = RGBColour.fromIntColour(0)
  val white: RGBColour        = RGBColour.fromIntColour(0xffffff)
  val red: RGBColour          = RGBColour.fromIntColour(0xff0000)
  val green: RGBColour        = RGBColour.fromIntColour(0x00ff00)
  val blue: RGBColour         = RGBColour.fromIntColour(0x0000ff)
  val yellow: RGBColour       = RGBColour.fromIntColour(0xffff00)
  val fuchsia: RGBColour      = RGBColour.fromIntColour(0xff00ff)
  val aqua: RGBColour         = RGBColour.fromIntColour(0x00ffff)
  val gray: RGBColour         = RGBColour.fromIntColour(0xc0c0c0)
  val orange: RGBColour       = RGBColour.fromIntColour(0xff9900)
  val brown: RGBColour        = RGBColour.fromIntColour(0x996633)
  val lightGreen: RGBColour   = RGBColour.fromIntColour(0x00cc99)
  val electricBlue: RGBColour = RGBColour.fromIntColour(0x6666ff)

  val someColoursForPlayers: Vector[RGBColour] = Vector(
    red,
    green,
    yellow,
    fuchsia,
    aqua,
    orange,
    lightGreen,
    electricBlue,
    gray
  )

  val coloursForPlayers: LazyList[RGBColour] = LazyList.continually(someColoursForPlayers).flatten

  val someColours: Vector[RGBColour] = Vector(
    red,
    green,
    blue,
    yellow,
    fuchsia,
    aqua,
    orange,
    brown,
    lightGreen,
    electricBlue
  )

  /** Creates an inifite [[LazyList]] of rotating colours. */
  def repeatedColours: LazyList[RGBColour] = LazyList.continually(someColours).flatten

}
