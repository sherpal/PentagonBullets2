package server.gamejoined

import actors.gamejoined.*
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import models.menus.{ClientToServer, GameJoinedInfo, PlayerName}
import server.websockethelpers.{flowThroughActor, webSocketService, PoisonPill}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Source}
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import org.slf4j.LoggerFactory
import io.circe.Codec

final class Routes()(using system: ActorSystem[_]) {

  private val logger = LoggerFactory.getLogger(getClass)

  private val gameJoinedRef = system.systemActorOf(GameJoined(), "GameJoined")

  def asRoute: Route =
    path("ws" / "game-joined") {
      parameter("player-name").map(name => PlayerName(name)).apply { (playerName: PlayerName) =>
        handleWebSocketMessages(
          webSocketService(
            Flow[ClientToServer]
              .map(ConnectionActor.fromClientToServer)
              .via(
                flowThroughActor(
                  (ref: ActorRef[GameJoinedInfo | PoisonPill]) =>
                    ConnectionActor(playerName, gameJoinedRef, gameJoinedRef, ref),
                  s"ConnectionActor${java.util.UUID.randomUUID()}",
                  ConnectionActor.Disconnect(),
                  _ => ConnectionActor.Disconnect()
                )
              )
          )
        )
      }
    }

}

object Routes {

  case object PoisonPill
  type OuterWorldCommand = GameJoinedInfo | PoisonPill.type

}
