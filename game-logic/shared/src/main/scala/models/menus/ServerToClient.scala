package models.menus

import io.circe.{Codec, Decoder, Encoder, Json, JsonObject}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import models.menus.GameKeys.GameKey

sealed trait ServerToClient

object ServerToClient {

  case class GameInfoWrapper(gameJoinedInfo: GameJoinedInfo) extends ServerToClient
  case object Heartbeat extends ServerToClient

  /** Sent to each player with their personal info when the game starts. */
  case class GameStarts(playerInfo: PlayerInfo, gameKey: GameKey) extends ServerToClient

  private val gameInfoWrapperEncoder = deriveEncoder[GameInfoWrapper]
  private val gameStartEncoder       = deriveEncoder[GameStarts]
  private val heartbeatEncoder       = Encoder.instance[Heartbeat.type](hb => Json.fromString(hb.toString))

  private val encoder: Encoder[ServerToClient] = Encoder.instance {
    case msg: GameInfoWrapper => gameInfoWrapperEncoder(msg)
    case msg: GameStarts      => gameStartEncoder(msg)
    case Heartbeat            => heartbeatEncoder(Heartbeat)
  }

  private val decoder: Decoder[ServerToClient] = List[Decoder[ServerToClient]](
    valueDecoder(Heartbeat).widen[ServerToClient],
    deriveDecoder[GameInfoWrapper].widen[ServerToClient],
    deriveDecoder[GameStarts].widen[ServerToClient]
  ).reduce(_ or _)

  given Codec[ServerToClient] = Codec.from(decoder, encoder)

}
