package server

import com.typesafe.config.Config
import zio.*

object ConfigReader {

  type WithConfig = Has[Config]

  def live(config: Config): ULayer[Has[Config]] = ZLayer.succeed(config)

  private val configM = ZIO.service[Config]

  def readString(path: String): ZIO[WithConfig, Throwable, String] = for {
    config <- configM
    string <- ZIO.effect(config.getString(path))
  } yield string

  def readInt(path: String): ZIO[WithConfig, Throwable, Int] = for {
    config <- configM
    int    <- ZIO.effect(config.getInt(path))
  } yield int

  def readBoolean(path: String): ZIO[WithConfig, Throwable, Boolean] = for {
    config <- configM
    bool   <- ZIO.effect(config.getBoolean(path))
  } yield bool

  val portM: ZIO[WithConfig, Throwable, Int]         = readInt("port")
  val hostM: ZIO[WithConfig, Throwable, String]      = readString("host")
  val prodModeM: ZIO[WithConfig, Throwable, Boolean] = readBoolean("prod")

}
