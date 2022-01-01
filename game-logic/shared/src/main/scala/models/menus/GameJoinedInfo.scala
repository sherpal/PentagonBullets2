package models.menus

import gamelogic.abilities.Ability
import io.circe.generic.semiauto.*
import io.circe.{Codec, Decoder, Encoder}

final case class GameJoinedInfo(players: Map[PlayerName, PlayerInfo]) {

  def withPlayer(playerInfo: PlayerInfo): GameJoinedInfo = copy(
    players = players + (playerInfo.name -> playerInfo)
  )

  def updateAbility(playerName: PlayerName, abilityId: Ability.AbilityId): GameJoinedInfo =
    players
      .get(playerName)
      .fold(this)(player => copy(players = players + (player.name -> player.changeAbility(abilityId))))

  def updateReadyStatus(playerName: PlayerName, status: Boolean): GameJoinedInfo =
    players
      .get(playerName)
      .fold(this)(player => copy(players = players + (player.name -> player.changeReadyStatus(status))))

  def withoutPlayer(playerName: PlayerName): GameJoinedInfo = copy(
    players = players - playerName
  )

  def obfuscated: GameJoinedInfo = copy(players = players.map((key, value) => (key, value.obfuscated)))

}

object GameJoinedInfo {

  implicit def mapCodec[K, V](using Codec[K], Codec[V]): Codec[Map[K, V]] = Codec.from(
    Decoder[List[(K, V)]].map(_.toMap),
    Encoder[List[(K, V)]].contramap(_.toList)
  )

  implicit val codec: Codec[GameJoinedInfo] = deriveCodec

}
