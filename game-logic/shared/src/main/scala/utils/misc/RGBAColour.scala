package utils.misc

final case class RGBAColour(red: Int, green: Int, blue: Int, alpha: Double) extends Colour {

  def withoutAlpha: RGBColour = RGBColour(red, green, blue)

  def withAlpha(newAlpha: Double): RGBAColour = copy(alpha = newAlpha)

  def asRGBAColour: RGBAColour = this

}

object RGBAColour {

  def blackTransparency(alpha: Double): Colour = RGBAColour(0, 0, 0, alpha)

}
