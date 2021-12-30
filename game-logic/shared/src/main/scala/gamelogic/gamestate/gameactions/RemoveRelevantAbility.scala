package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity, WithAbilities}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.abilities.Ability
import gamelogic.gamestate.statetransformers.*

final case class RemoveRelevantAbility(
    actionId: GameAction.Id,
    time: Long,
    entityId: Entity.Id,
    useId: Ability.UseId,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[WithAbilities](entityId, time, _.removeRelevantAbility(useId, time))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
