package ziojs

import zio.ZIO

object Implicits {
  implicit class ZIOJS[R, E, A](val effect: ZIO[R, E, A]) extends AnyVal {
    def whenInDev(implicit ev: A <:< Unit): ZIO[R, E, Unit] = {
      type ToLift[+X] = ZIO[R, E, X]
      whenDev(ev.liftCo[ToLift](effect))
    }
  }
}
