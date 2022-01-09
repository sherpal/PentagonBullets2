package gamelogic.gamestate.serveractions
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.gamestate.gameactions.GameEnded
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer

object ManageEndOfGame extends ServerActionFromActionList {

  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    println(getClass)

    val time      = nowGenerator()
    val gameState = currentState.currentGameState
    val teamsLeft = gameState.players.values.map(_.team).toList.distinct.size

    if teamsLeft <= 1 then List(GameEnded(actionId(), time, ServerSource))
    else Nil
  }
}
