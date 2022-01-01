import zio._

package object frontend {

  type GlobalEnv = zio.console.Console

  val runtime = Runtime.default.unsafeRun(
    ZIO
      .runtime[GlobalEnv]
      .provideLayer(
        zio.console.Console.live
      )
  )

}
