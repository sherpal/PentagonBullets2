package entitiescollections

import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.GameState

/** A PlayerTeam is a group of [[Player]]s that are together. Bad actions from a player does not affect other players in
  * the team. Example: no collision between a bullet from player A with player B if they belong to the same team. Good
  * actions from a player does affect other players in the team.
  *
  * A Player can NOT belong to more than one team.
  */
final class PlayerTeam(val teamNbr: Int, val playerIds: Seq[Entity.Id]) {

  val nbrOfPlayers: Int = playerIds.length

  def leader: Entity.Id = playerIds.head

  def contains(player: Player): Boolean = contains(player.id)

  def contains(playerId: Entity.Id): Boolean = playerIds.contains[Entity.Id](playerId)

  def alive(gameState: GameState): Boolean = playerIds.exists(gameState.players.isDefinedAt)

}
