package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Obstacle
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

/** Add a new [[Obstacle]] to the game.
  */
final case class NewObstacle(
    actionId: GameAction.Id,
    time: Long,
    id: Entity.Id,
    pos: Complex,
    vertices: Vector[Complex],
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(Obstacle(id, pos, vertices), time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
