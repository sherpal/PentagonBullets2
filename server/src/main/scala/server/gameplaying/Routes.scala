package server.gameplaying

import akka.actor.typed.{ActorRef, ActorSystem}
import actors.gameplaying.{ConnectionActor, GamePlaying, GamePlayingKeeper}
import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import models.menus.GameKeys.GameKey
import models.menus.{GameJoinedInfo, PlayerName}
import org.slf4j.LoggerFactory
import server.websockethelpers.{binaryWebSocketService, flowThroughActor, heartbeat, PoisonPill}
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import gamecommunication.{ClientToServer, ServerToClient}

import scala.concurrent.duration.*
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
            case Success(Some(gamePlayingRef)) =>
              logger.info(s"New connection for game $gameKey ($playerName)")
              handleWebSocketMessages(
                binaryWebSocketService[ClientToServer, ServerToClient, NotUsed](
                  Flow[ClientToServer]
                    .map(ConnectionActor.fromClientToServer)
                    .via(
                      flowThroughActor(
                        (ref: ActorRef[ServerToClient | PoisonPill]) =>
                          ConnectionActor(playerName, gamePlayingRef, ref),
                        s"ConnectionActor${java.util.UUID.randomUUID()}",
                        ConnectionActor.Disconnect,
                        error => {
                          logger.error("Error in actor flow", error)
                          ConnectionActor.Disconnect
                        }
                      )
                    )
                    .merge(heartbeat(ServerToClient.Heartbeat, 5.seconds))
                )
              )
          }
      }
    }
  }
}
