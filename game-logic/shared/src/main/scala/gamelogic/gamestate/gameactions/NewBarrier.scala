package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Barrier
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

final case class NewBarrier(
    actionId: GameAction.Id,
    time: Long,
    id: Entity.Id,
    ownerId: Entity.Id,
    teamId: Int,
    pos: Complex,
    rotation: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(Barrier(id, time, ownerId, teamId, pos.re, pos.im, rotation, Barrier.shape), time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
