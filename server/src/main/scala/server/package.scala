import com.typesafe.config.ConfigFactory
import zio.ZIO

package object server {

  type GlobalEnv = ConfigReader.WithConfig

  val runtime: zio.Runtime[GlobalEnv] = zio.Runtime.default.unsafeRun(
    ZIO
      .runtime[GlobalEnv]
      .provideLayer(ConfigReader.live(ConfigFactory.load()))
  )

}
