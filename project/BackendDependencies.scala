import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Def.settings
import sbt._
import sbt.Keys.libraryDependencies

//noinspection TypeAnnotation
object BackendDependencies {
  val AkkaVersion     = "2.6.18"
  val AkkaHttpVersion = "10.2.6"

  def addDependencies() = settings(
    libraryDependencies ++= List(
      "com.typesafe.akka" % "akka-actor-typed_2.13" % AkkaVersion,
      "com.typesafe.akka" % "akka-stream-typed_2.13" % AkkaVersion,
      "com.typesafe.akka" % "akka-stream_2.13" % AkkaVersion,
      "com.typesafe.akka" % "akka-http_2.13" % AkkaHttpVersion,
      "com.typesafe" % "config" % "1.4.1",
      "ch.qos.logback" % "logback-classic" % "1.2.10"
    )
  )

}
