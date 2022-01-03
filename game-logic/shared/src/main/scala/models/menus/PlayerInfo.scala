package models.menus

import gamelogic.abilities.Ability
import gamelogic.entities.Entity.TeamId
import io.circe.generic.semiauto.*
import io.circe.{Codec, Decoder, Encoder}
import models.gamecodecs.CirceCodecs.*

final case class PlayerInfo(
    name: PlayerName,
    ability: Ability.AbilityId,
    readyStatus: Boolean,
    teamId: TeamId,
    isObfuscated: Boolean
) {
  def obfuscated: PlayerInfo = copy(isObfuscated = true)

  def changeAbility(abilityId: Ability.AbilityId): PlayerInfo = copy(
    ability = abilityId
  )

  def changeReadyStatus(status: Boolean): PlayerInfo = copy(
    readyStatus = status
  )

  def changeTeamId(teamId: TeamId): PlayerInfo = copy(teamId = teamId)

  def allowedAbilities: List[Ability.AbilityId] = List(ability, Ability.activateShieldId)
}

object PlayerInfo {

  private val encoder: Encoder[PlayerInfo] = Encoder.instance[PlayerInfo] { (playerInfo: PlayerInfo) =>
    val defaultEncoder = deriveEncoder[PlayerInfo]
    val encoder =
      if playerInfo.isObfuscated then defaultEncoder.mapJsonObject(json => json.filter((key, _) => key != "ability"))
      else defaultEncoder

    encoder(playerInfo)
  }

  private case class ObfuscatedPlayerInfo(
      name: PlayerName,
      readyStatus: Boolean,
      teamId: TeamId
  )

  private val decoder: Decoder[PlayerInfo] =
    deriveDecoder[PlayerInfo] or deriveDecoder[ObfuscatedPlayerInfo].map(obfuscated =>
      PlayerInfo(obfuscated.name, Ability.bigBulletId, obfuscated.readyStatus, obfuscated.teamId, true)
    )

  implicit val codec: Codec[PlayerInfo] = Codec.from(decoder, encoder)

  def init(playerName: PlayerName, teamId: TeamId): PlayerInfo = PlayerInfo(
    playerName,
    Ability.bigBulletId,
    false,
    teamId,
    false
  )

}
