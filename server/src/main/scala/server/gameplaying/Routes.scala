package server.gameplaying

import akka.actor.typed.{ActorRef, ActorSystem}
import actors.gameplaying.{GamePlaying, GamePlayingKeeper}
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import models.menus.GameKeys.GameKey
import models.menus.{ClientToServer, GameJoinedInfo, PlayerName, ServerToClient}
import org.slf4j.LoggerFactory
import server.websockethelpers.{flowThroughActor, heartbeat, webSocketService, PoisonPill}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.Future
import scala.util.{Failure, Success}

final class Routes(gamePlayingKeeperRef: ActorRef[GamePlayingKeeper.Command])(using system: ActorSystem[_]) {

  private val logger = LoggerFactory.getLogger(getClass)

  given Timeout = Timeout(3.seconds)

  def refForGame(gameKey: GameKey): Future[Option[ActorRef[GamePlaying.Command]]] =
    gamePlayingKeeperRef.ask(GamePlayingKeeper.GamePlayingRefPlease(gameKey, _))

  def asRoute: Route = path("ws" / "in-game") {
    parameter("player-name").map(name => PlayerName(name)).apply { (playerName: PlayerName) =>
      parameter("game-key").map(GameKey.fromString).apply {
        case Left(error) =>
          logger.error("Malformed key", error)
          complete(StatusCodes.BadRequest, "Malformed key")
        case Right(gameKey) =>
          onComplete(refForGame(gameKey)) {
            case Failure(error) =>
              logger.error("Failed to retrieve game playing actor ref", error)
              complete(StatusCodes.InternalServerError)
            case Success(None) =>
              complete(StatusCodes.NotFound, "Game key did not match")
            case Success(Some(ref)) =>
              logger.info(s"New connection for game $gameKey")
              handleWebSocketMessages(???)
          }
      }
    }
  }
}
