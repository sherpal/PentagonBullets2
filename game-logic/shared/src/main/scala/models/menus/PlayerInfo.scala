package models.menus

import gamelogic.abilities.Ability
import io.circe.generic.semiauto.*
import io.circe.Codec
import models.gamecodecs.CirceCodecs.*

final case class PlayerInfo(name: PlayerName, ability: Option[Ability.AbilityId], readyStatus: Boolean) {
  def obfuscated: PlayerInfo = copy(ability = None)

  def changeAbility(abilityId: Ability.AbilityId): PlayerInfo = copy(
    ability = Some(abilityId)
  )

  def changeReadyStatus(status: Boolean): PlayerInfo = copy(
    readyStatus = status
  )
}

object PlayerInfo {
  implicit val codec: Codec[PlayerInfo] = deriveCodec

  def init(playerName: PlayerName): PlayerInfo = PlayerInfo(
    playerName,
    Some(Ability.bigBulletId),
    false
  )

}
