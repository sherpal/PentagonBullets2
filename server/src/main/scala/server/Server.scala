package server

import actors.gamejoined.GameJoined
import actors.gameplaying.GamePlayingKeeper
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import server.gamejoined.Routes

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success}

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "PentagonBullet")
    given ExecutionContextExecutor            = system.executionContext

    println(system.settings.config)

    val (port, host, prodMode) = runtime.unsafeRun(for {
      p    <- ConfigReader.portM
      h    <- ConfigReader.hostM
      prod <- ConfigReader.prodModeM
    } yield (p, h, prod))

    val gamePlayingKeeperRef = system.systemActorOf(GamePlayingKeeper(), "GamePlayingKeeper")
    val gameJoinedRef        = system.systemActorOf(GameJoined(gamePlayingKeeperRef), "GameJoined")

    val staticRoute = path(Segments) {
      case Nil => reject // skip to public/index.html
      case segments =>
        getFromResource(("public" +: segments).mkString("/"))
    } ~ getFromResource("public/index.html")

    val gameJoinedRoutes  = new Routes(gameJoinedRef).asRoute
    val gamePlayingRoutes = new server.gameplaying.Routes(gamePlayingKeeperRef).asRoute

    val routes = gameJoinedRoutes ~ gamePlayingRoutes ~ staticRoute

    val bindingFuture = Http()
      .newServerAt(host, port)
      .bind(routes)

    println(s"Server online at http://$host:$port/")
    println(if prodMode then "Running in production mode..." else "Running in dev mode...")

    if !prodMode then println("Press ENTER to stop.")

  }

}
