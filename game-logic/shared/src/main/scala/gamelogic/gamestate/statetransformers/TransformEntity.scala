package gamelogic.gamestate.statetransformers

import gamelogic.entities.Entity
import gamelogic.gamestate.GameState

import scala.reflect.ClassTag

final class TransformEntity[T <: Entity](id: Entity.Id, time: Long, f: T => T)(using ClassTag[T])
    extends GameStateTransformer {

  def apply(gameState: GameState): GameState =
    gameState.entityByIdAs[T](id).map(f).fold(gameState)(gameState.withEntity(id, time, _))

}
