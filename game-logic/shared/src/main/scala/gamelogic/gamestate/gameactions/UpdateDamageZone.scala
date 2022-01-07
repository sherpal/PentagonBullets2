package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Circle
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.DamageZone
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Updates (or creates) the DamageZone at the specified position and with the specified radius.
  */
final case class UpdateDamageZone(
    actionId: GameAction.Id,
    time: Long,
    id: Entity.Id,
    lastGrow: Long,
    lastTick: Long,
    pos: Complex,
    radius: Int,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(DamageZone(id, lastGrow, lastTick, pos, new Circle(radius)), time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
