package models.menus

import io.circe.{Codec, Decoder, Encoder, Json}
import io.circe.generic.semiauto.*
import gamelogic.abilities.Ability
import gamelogic.entities.Entity.TeamId
import models.gamecodecs.CirceCodecs.abilityIdCodec

sealed trait ClientToServer

object ClientToServer {

  case class SelectAbilityId(abilityId: Ability.AbilityId) extends ClientToServer
  case class ChangeReadyStatus(ready: Boolean) extends ClientToServer
  case class ChangeTeamId(teamId: TeamId) extends ClientToServer
  case object Disconnect extends ClientToServer
  case object StartGame extends ClientToServer

  private val encoder: Encoder[ClientToServer] = Encoder.instance {
    case msg: SelectAbilityId   => deriveEncoder[SelectAbilityId].apply(msg)
    case msg: ChangeReadyStatus => deriveEncoder[ChangeReadyStatus].apply(msg)
    case msg: ChangeTeamId      => deriveEncoder[ChangeTeamId].apply(msg)
    case Disconnect             => Json.fromString(Disconnect.toString)
    case StartGame              => Json.fromString(StartGame.toString)
  }

  private val decoder: Decoder[ClientToServer] = List[Decoder[ClientToServer]](
    valueDecoder(Disconnect).widen[ClientToServer],
    valueDecoder(StartGame).widen[ClientToServer],
    deriveDecoder[ChangeReadyStatus].widen[ClientToServer],
    deriveDecoder[ChangeTeamId].widen[ClientToServer],
    deriveDecoder[SelectAbilityId].widen[ClientToServer]
  ).reduce(_ or _)

  given Codec[ClientToServer] = Codec.from(decoder, encoder)
}
