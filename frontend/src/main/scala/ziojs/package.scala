import zio._

package object ziojs {

  def whenDev[R, E](effect: ZIO[R, E, Unit]): ZIO[R, E, Unit] =
    ZIO.when(scala.scalajs.LinkingInfo.developmentMode)(effect)

}
