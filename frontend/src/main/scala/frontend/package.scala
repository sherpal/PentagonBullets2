import zio._

package object frontend {

  type GlobalEnv = zio.console.Console with zio.clock.Clock

  /** This is a bit ugly but we don't have a choice since initializing some components require to be async. */
  private var _runtime: Option[Runtime[GlobalEnv]] = None

  val initialiseRuntime: UIO[Unit] = for {
    theRuntime <- ZIO
      .runtime[GlobalEnv]
      .provideLayer(
        zio.console.Console.live ++ zio.clock.Clock.live
      )
  } yield _runtime = Some(theRuntime)

  def runtime: Runtime[GlobalEnv] = _runtime.get

}
