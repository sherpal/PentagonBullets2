package models.menus

import io.circe.{Codec, Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import models.menus.GameKeys.GameKey

sealed trait ServerToClient

object ServerToClient {

  case class GameInfoWrapper(gameJoinedInfo: GameJoinedInfo) extends ServerToClient
  case object Heartbeat extends ServerToClient

  /** Sent to each player with their personal info when the game starts. */
  case class GameStarts(playerInfo: PlayerInfo, gameKey: GameKey) extends ServerToClient

  private val encoder: Encoder[ServerToClient] = Encoder.instance {
    case msg: GameInfoWrapper => deriveEncoder[GameInfoWrapper].apply(msg)
    case msg: GameStarts      => deriveEncoder[GameStarts].apply(msg)
    case Heartbeat            => Json.fromString(Heartbeat.toString)
  }

  private val decoder: Decoder[ServerToClient] = List[Decoder[ServerToClient]](
    valueDecoder(Heartbeat).widen[ServerToClient],
    deriveDecoder[GameInfoWrapper].widen[ServerToClient],
    deriveDecoder[GameStarts].widen[ServerToClient]
  ).reduce(_ or _)

  given Codec[ServerToClient] = Codec.from(decoder, encoder)

}
