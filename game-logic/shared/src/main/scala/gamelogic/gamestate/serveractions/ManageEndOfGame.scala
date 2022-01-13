package gamelogic.gamestate.serveractions
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.gamestate.gameactions.{GameEnded, UpdatePlayerPos}
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer

object ManageEndOfGame extends ServerActionFromActionList {

  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    val time      = nowGenerator()
    val gameState = currentState.currentGameState
    val players   = gameState.players.values
    val teamsLeft = players.map(_.team).toSet.size

    if teamsLeft <= 1 then
      List(GameEnded(actionId(), time, ServerSource)) ++ players
        .filter(_.moving)
        .map(player =>
          UpdatePlayerPos(
            actionId(),
            time,
            player.id,
            player.currentPosition(time),
            player.direction,
            moving = false,
            rot = player.rotation,
            ServerSource
          )
        )
    else Nil
  }
}
