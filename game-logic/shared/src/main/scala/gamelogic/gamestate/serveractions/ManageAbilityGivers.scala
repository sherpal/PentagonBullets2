package gamelogic.gamestate.serveractions

import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer
import gamelogic.gamestate.gameactions.*
import gamelogic.entities.ActionSource.ServerSource

object ManageAbilityGivers extends ServerActionFromActionList {
  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    val time          = nowGenerator()
    val gameState     = currentState.currentGameState
    val abilityGivers = gameState.abilityGivers

    abilityGivers.values.flatMap(abilityGiver =>
      gameState.players.values.find(player => abilityGiver.collides(player, time)).map { player =>
        PlayerTakeAbilityGiver(
          actionId(),
          time,
          player.id,
          abilityGiver.id,
          abilityGiver.abilityId,
          ServerSource
        )
      }
    )
  }
}
