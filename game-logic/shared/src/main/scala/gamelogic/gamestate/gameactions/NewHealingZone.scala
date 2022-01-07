package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.HealingZone
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}
import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Circle

/** Happens when a new HealingZone is created.
  */
final case class NewHealingZone(
    actionId: GameAction.Id,
    time: Long,
    zoneId: Entity.Id,
    ownerId: Entity.Id,
    lifeSupply: Double,
    pos: Complex,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(HealingZone(zoneId, time, ownerId, time, lifeSupply, pos, new Circle(HealingZone.radius)), time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
