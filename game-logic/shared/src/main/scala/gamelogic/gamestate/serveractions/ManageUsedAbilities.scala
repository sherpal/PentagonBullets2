package gamelogic.gamestate.serveractions

import gamelogic.entities.ActionSource.ServerSource
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.gamestate.gameactions.UseAbilityAction
import gamelogic.utils.IdGeneratorContainer
import gamelogic.abilities.Ability

object ManageUsedAbilities extends ServerAction {
  def apply(
      currentState: ActionGatherer,
      nowGenerator: () => Long
  )(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): (ActionGatherer, ServerAction.ServerActionOutput) = {
    val startTime = nowGenerator()
    val gameState = currentState.currentGameState

    val usedAbilities = gameState.castingEntityInfo.valuesIterator
      .filter(castingInfo => startTime - castingInfo.startedTime >= castingInfo.ability.castingTime)
      .map { castingInfo =>
        val useId = Ability.nextUseId()
        println("an ability!")
        UseAbilityAction(
          GameAction.newId(),
          startTime,
          castingInfo.ability.copyWithNewTimeAndId(startTime, useId),
          useId,
          ServerSource
        )
      }
      .flatMap(usage => usage :: usage.ability.createActions(gameState))
      .toList

    val (nextCollector, oldestTime, idsToRemove) = currentState.masterAddAndRemoveActions(usedAbilities)

    (nextCollector, ServerAction.ServerActionOutput(usedAbilities, oldestTime, idsToRemove))
  }
}
