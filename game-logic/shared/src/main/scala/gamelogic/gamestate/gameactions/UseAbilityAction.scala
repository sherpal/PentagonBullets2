package gamelogic.gamestate.gameactions

import gamelogic.abilities.Ability
import gamelogic.entities.{ActionSource, WithAbilities}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class UseAbilityAction(
    actionId: GameAction.Id,
    time: Long,
    ability: Ability,
    useId: Ability.UseId,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[WithAbilities](ability.casterId, time, _.useAbility(ability.copyWithNewTimeAndId(time, useId)))

  override def canHappen(gameState: GameState): Boolean = gameState.isPlayerAlive(ability.casterId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
