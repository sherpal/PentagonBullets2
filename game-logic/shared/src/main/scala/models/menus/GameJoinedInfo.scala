package models.menus

import gamelogic.abilities.Ability
import gamelogic.entities.Entity.TeamId
import io.circe.generic.semiauto.*
import io.circe.{Codec, Decoder, Encoder}

final case class GameJoinedInfo(players: Map[PlayerName, PlayerInfo], maybeLeader: Option[PlayerName]) {

  def withPlayer(playerInfo: PlayerInfo): GameJoinedInfo = copy(
    players = players + (playerInfo.name -> playerInfo),
    maybeLeader = maybeLeader.orElse(Some(playerInfo.name))
  )

  private def updatePlayer(playerName: PlayerName, update: PlayerInfo => PlayerInfo): GameJoinedInfo =
    players.get(playerName).map(update).fold(this)(this.withPlayer)

  def updateAbility(playerName: PlayerName, abilityId: Ability.AbilityId): GameJoinedInfo =
    updatePlayer(playerName, _.changeAbility(abilityId))

  def updateReadyStatus(playerName: PlayerName, status: Boolean): GameJoinedInfo =
    updatePlayer(playerName, _.changeReadyStatus(status))

  def updateTeamId(playerName: PlayerName, teamId: TeamId): GameJoinedInfo =
    updatePlayer(playerName, _.changeTeamId(teamId))

  def withoutPlayer(playerName: PlayerName): GameJoinedInfo = {
    val newPlayers        = players - playerName
    val perhapsNextLeader = newPlayers.headOption.map(_._1)
    copy(
      players = newPlayers,
      maybeLeader = (for {
        currentLeader <- maybeLeader
        if newPlayers contains currentLeader
      } yield currentLeader).orElse(perhapsNextLeader)
    )
  }

  def obfuscated: GameJoinedInfo = copy(players = players.map((key, value) => (key, value.obfuscated)))

  def allTeamIds: Set[TeamId] = players.map(_._2.teamId).toSet

  def canStart: Boolean = players.forall(_._2.readyStatus) && allTeamIds.size > 1

  def isLeader(playerName: PlayerName): Boolean = maybeLeader.contains[PlayerName](playerName)

  def firstUnusedTeamId: Int = LazyList.from(1).find(teamId => !allTeamIds.contains(teamId)).get // this must terminate

  def allTeamIdsWithUnused: List[TeamId] = (allTeamIds + firstUnusedTeamId).toList.sorted

}

object GameJoinedInfo {

  implicit def mapCodec[K, V](using Codec[K], Codec[V]): Codec[Map[K, V]] = Codec.from(
    Decoder[List[(K, V)]].map(_.toMap),
    Encoder[List[(K, V)]].contramap(_.toList)
  )

  implicit val codec: Codec[GameJoinedInfo] = deriveCodec

}
