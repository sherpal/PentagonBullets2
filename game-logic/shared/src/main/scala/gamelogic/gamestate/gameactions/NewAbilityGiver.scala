package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.abilities.Ability
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.AbilityGiver
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

final case class NewAbilityGiver(
    actionId: GameAction.Id,
    time: Long,
    abilityGiverId: Entity.Id,
    pos: Complex,
    abilityId: Ability.AbilityId,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(AbilityGiver(abilityGiverId, time, pos.re, pos.im, abilityId), time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
