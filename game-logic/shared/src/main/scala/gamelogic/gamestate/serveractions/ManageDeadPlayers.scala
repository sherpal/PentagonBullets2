package gamelogic.gamestate.serveractions
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.gamestate.gameactions.PlayerDead
import gamelogic.utils.IdGeneratorContainer

object ManageDeadPlayers extends ServerAction {
  def apply(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): (ActionGatherer, ServerAction.ServerActionOutput) = {
    val now = nowGenerator()

    val actions = currentState.currentGameState.players.values
      .filter(_.lifeTotal <= 0)
      .map(player => PlayerDead(GameAction.newId(), now, player.id, player.name, ServerSource))
      .toList

    val (nextCollector, oldestTime, idsToRemove) = currentState.masterAddAndRemoveActions(actions)

    (nextCollector, ServerAction.ServerActionOutput(actions, oldestTime, idsToRemove))
  }

}
