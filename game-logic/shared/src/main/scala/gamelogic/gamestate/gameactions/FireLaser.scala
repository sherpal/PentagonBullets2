package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.statetransformers.GameStateTransformer
import gamelogic.gamestate.{GameAction, GameState}

final case class FireLaser(
    actionId: GameAction.Id,
    time: Long,
    ownerId: Entity.Id,
    laserVertices: Vector[Complex],
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer = GameStateTransformer.identityTransformer

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

}
