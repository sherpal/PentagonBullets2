package game.ui.effects

import be.doeraene.physics.Complex

import scala.util.Random

/** A [[Path]] is basically a function from time (Long) to a position in the [[gamelogic.physics.Complex]] plane.
  *
  * It has facility methods for creating and composing different paths.
  */
trait Path extends (Long => Complex) {

  /** Specifies the duration that this path will last. Is set to None, the duration is considered to be infinite.
    */
  def maybeDuration: Option[Long]

  def isFinite: Boolean = maybeDuration.isDefined

  def isOver(time: Long): Boolean = maybeDuration.fold(false)(time > _)

  /** Reverse the path. */
  def unary_- : Path = Path.factory(maybeDuration, t => -apply(t))

  /** Translates the path. */
  def +(z: Complex): Path = Path.Translated(this, z)

  /** Apply the conformal map */
  def *(z: Complex): Path = Path.Mult(this, z)

  /** Rotates the path by the given angle. */
  def rotation(angle: Double): Path = this * Complex.rotation(angle)

  /** Applies a random rotation with angle between `-maxAngle` and `maxAngle` */
  def jitter(maxAngle: Double): Path = rotation(Random.between(-maxAngle, maxAngle))

  /** Follows `this` path and then `that` path. If `this` path is infinite, then `that` path is never followed.
    */
  def ++(that: Path): Path = Path.Concat(this, that)

  def stopAfter(time: Long): Path = Path.WithTimeLimit(this, time)

}

object Path {

  trait InfinitePath extends Path {
    def maybeDuration: Option[Long] = None
  }

  def factory(duration: Option[Long], path: Long => Complex): Path = new Path {
    def maybeDuration: Option[Long] = duration
    def apply(t: Long): Complex     = path(t)
  }

  def infiniteFactory(path: Long => Complex): Path = new Path {
    def maybeDuration: Option[Long] = None
    def apply(time: Long): Complex  = path(time)
  }

  def finiteFactory(duration: Long, path: Long => Complex): Path = new Path {
    def maybeDuration: Option[Long] = Some(duration)
    def apply(time: Long): Complex  = path(time)
  }

  case object PositiveRealLine extends InfinitePath {
    def apply(time: Long): Complex = time
  }
  val positiveRealLine: Path = PositiveRealLine

  def positiveSegment(duration: Long): Path = positiveRealLine.stopAfter(duration)

  def segment(duration: Long, angle: Double, speed: Double): Path =
    positiveSegment(duration) * (Complex.rotation(angle) * (speed / 1000))

  def goUp(duration: Long, speed: Double): Path    = segment(duration, math.Pi / 2, speed)
  def goDown(duration: Long, speed: Double): Path  = -goUp(duration, speed)
  def goRight(duration: Long, speed: Double): Path = positiveSegment(duration) * speed
  def goLeft(duration: Long, speed: Double): Path  = -goRight(duration, speed)

  private case class CircleLoop(radius: Double, loopDuration: Long) extends InfinitePath {
    def apply(time: Long): Complex = radius * Complex.rotation(2 * math.Pi * time / loopDuration)
  }
  def circleLoop(radius: Double, loopDuration: Long): Path = CircleLoop(radius, loopDuration)

  def circle(duration: Long, radius: Double): Path = circleLoop(radius, duration).stopAfter(duration)

  def arc(duration: Long, radius: Double, fromAngle: Double, toAngle: Double): Path =
    finiteFactory(
      duration,
      t =>
        radius * Complex.rotation(
          fromAngle + t * (toAngle - fromAngle) / duration.toDouble
        )
    )

  private case class Translated(path: Path, translation: Complex) extends Path {
    def apply(time: Long): Complex  = path(time) + translation
    def maybeDuration: Option[Long] = path.maybeDuration
  }

  private case class Mult(path: Path, multiplier: Complex) extends Path {
    def apply(time: Long): Complex  = path(time) * multiplier
    def maybeDuration: Option[Long] = path.maybeDuration
  }

  private case class Concat(first: Path, second: Path) extends Path {
    private val f = first.maybeDuration match {
      case None => first.apply
      case Some(duration) =>
        val endPosition      = first(duration)
        val secondTranslated = second + endPosition
        (time: Long) => if time <= duration then first(time) else secondTranslated(time - duration)
    }

    def apply(time: Long): Complex = f(time)
    def maybeDuration: Option[Long] = for {
      firstDuration  <- first.maybeDuration
      secondDuration <- second.maybeDuration
    } yield firstDuration + secondDuration
  }

  private case class WithTimeLimit(path: Path, limit: Long) extends Path {
    def maybeDuration: Option[Long] = Some(path.maybeDuration.fold(limit)(_ min limit))
    def apply(time: Long): Complex  = path(time)
  }

}
