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

lazy val `server` = project
  .in(file("./server"))
  .settings(commonSettings)
  .settings(BackendDependencies.addDependencies())
  .dependsOn(`game-logic`.jvm)

lazy val cypress = project
  .in(file("./cypress"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.1.0"
  )
  .dependsOn(`game-logic`.js)

lazy val frontend = project
  .in(file("./frontend"))
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= List(
      "com.raquo" %%% "laminar" % "0.13.0"
    ),
    stUseScalaJsDom := false,
    stIgnore := List(
      "@pixi/constants",
      "@pixi/core",
      "@pixi/math",
      "@pixi/settings",
      "@pixi/utils",
      "tailwindcss"
    ),
    externalNpm := {
      scala.sys.process.Process("npm", baseDirectory.value).!
      baseDirectory.value
    },
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(`game-logic`.js)

val compileCypress =
  taskKey[Unit]("fastOptJS and copy-paste inside cypress directory.")

compileCypress := {
  val target = (cypress / Compile / fastOptJS).value.data

  IO.copyFile(
    target,
    baseDirectory.value / "cypress" / "cypress" / "integration" / "scala" / target.getName,
    CopyOptions().withOverwrite(true)
  )

  println("[info] Done.")
}
