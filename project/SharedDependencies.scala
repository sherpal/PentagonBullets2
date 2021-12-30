import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.settings
import sbt._
import sbt.Keys.libraryDependencies

object SharedDependencies {

  val circeVersion = "0.14.1"

  def addDependencies() = settings(
    libraryDependencies ++= List(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion) ++ List(
      "be.doeraene" %%% "url-dsl" % "0.4.0",
      "dev.zio" %%% "zio" % "1.0.8",
      "io.suzaku" %%% "boopickle" % "1.4.0"
    )
  )

}
