import sbt.Keys._
import sbtcrossproject.CrossPlugin.autoImport.crossProject

ThisBuild / version := "0.1.0-SNAPSHOT"

val dottyVersion = "3.1.0"
name := "PentagonBullets2"
version := "1.0.0"

val commonSettings = List(
  scalaVersion := dottyVersion,
  libraryDependencies += "org.scalameta" %%% "munit" % "0.7.26" % Test,
  libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.15.3" % Test,
  libraryDependencies ++= List(
    "com.softwaremill.magnolia1_3" %%% "magnolia" % "1.0.0"
  ),
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked",
    "-language:higherKinds"
  )
)

lazy val `game-logic` = crossProject(JSPlatform, JVMPlatform)
  .in(file("./game-logic"))
  .settings(commonSettings)
  .settings(
    SharedDependencies.addDependencies()
  )
  .jsSettings(
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.3.0",
    //libraryDependencies += "io.github.cquiroz" %%% "scala-java-time-tzdb_sjs1_2.13" % "2.3.0",
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )
