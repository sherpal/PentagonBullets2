package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.{AbilityGiver, Player}
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.abilities.Ability
import gamelogic.gamestate.statetransformers.*

/** A Player takes the AbilityGiver, removing it from the game, and then receives the ability.
  */
final case class PlayerTakeAbilityGiver(
    actionId: GameAction.Id,
    time: Long,
    playerId: Entity.Id,
    abilityGiverId: Entity.Id,
    abilityId: Ability.AbilityId,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    RemoveEntity(abilityGiverId, time) ++ GameStateTransformer.maybe(
      gameState
        .entityByIdAs[AbilityGiver](abilityGiverId)
        .map(_.abilityId)
        .map(abilityId =>
          new TransformEntity[Player](playerId, time, AbilityGiver.playerTakeAbilityGiver(_, time, abilityId))
        )
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
