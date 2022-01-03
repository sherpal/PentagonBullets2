package server.gamejoined

import actors.gamejoined.*
import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import models.menus.{ClientToServer, GameJoinedInfo, PlayerName, ServerToClient}
import server.websockethelpers.{flowThroughActor, heartbeat, webSocketService, PoisonPill}
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import io.circe.Codec

import scala.concurrent.duration.*

final class Routes(gameJoinedRef: ActorRef[GameJoined.Command])(using system: ActorSystem[_]) {

  def asRoute: Route =
    path("ws" / "game-joined") {
      parameter("player-name").map(name => PlayerName(name)).apply { (playerName: PlayerName) =>
        handleWebSocketMessages(
          webSocketService[ClientToServer, ServerToClient, NotUsed](
            Flow[ClientToServer]
              .map(ConnectionActor.fromClientToServer)
              .via(
                flowThroughActor(
                  (ref: ActorRef[ServerToClient | PoisonPill]) =>
                    ConnectionActor(playerName, gameJoinedRef, gameJoinedRef, ref),
                  s"ConnectionActor${java.util.UUID.randomUUID()}",
                  ConnectionActor.Disconnect(),
                  _ => ConnectionActor.Disconnect()
                )
              )
              .merge(heartbeat(ServerToClient.Heartbeat, 5.seconds))
          )
        )
      }
    }

}
