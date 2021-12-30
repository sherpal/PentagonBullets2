package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Mist
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Updates an existing Mist or create an other one.
  */
final case class UpdateMist(
    actionId: GameAction.Id,
    time: Long,
    id: Entity.Id,
    lastGrow: Long,
    lastTick: Long,
    sideLength: Double,
    gameAreaSideLength: Int,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    // Cutting into two cases will boosts performance
    gameState.entityByIdAs[Mist](id) match {
      case Some(mist) if mist.lastTick != lastTick =>
        // We are updating the lastTick, no need to recreate a shape
        WithEntity(Mist(id, lastGrow, lastTick, mist.shape), time)
      case _ =>
        // Either it's a new Mist, or we update the size. Either way, we need a new shape.
        WithEntity(Mist(id, lastGrow, lastTick, Mist.makeMistShape(sideLength, gameAreaSideLength)), time)
    }

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
