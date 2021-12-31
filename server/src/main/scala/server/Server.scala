package server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.util.{Failure, Success}

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "PentagonBullet")
    given ExecutionContextExecutor            = system.executionContext

    val (port, host, prodMode) = runtime.unsafeRun(for {
      p    <- ConfigReader.portM
      h    <- ConfigReader.hostM
      prod <- ConfigReader.prodModeM
    } yield (p, h, prod))

    val staticRoute = path(Segments) {
      case Nil => reject // skip to public/index.html
      case segments =>
        getFromResource(("public" +: segments).mkString("/"))
    } ~ getFromResource("public/index.html")

    val routes = path("hello")(complete(StatusCodes.OK, "hello")) ~ staticRoute

    val bindingFuture = Http()
      .newServerAt(host, port)
      .bind(routes)

    println(s"Server online at http://$host:$port/")
    println(if prodMode then "Running in production mode..." else "Running in dev mode...")

    if !prodMode then println("Press ENTER to stop.")

  }

}
