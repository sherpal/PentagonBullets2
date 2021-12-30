package gamelogic.gamestate.gameactions

import gamelogic.gamestate.{GameAction, GameState}
import be.doeraene.physics.shape.Polygon
import gamelogic.entities.ActionSource
import gamelogic.gamestate.statetransformers.{GameBegins => GameBeginsTransformer, GameStateTransformer}

final case class GameBegins(
    actionId: GameAction.Id,
    time: Long,
    gameBounds: Polygon,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    GameBeginsTransformer(time, gameBounds)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
