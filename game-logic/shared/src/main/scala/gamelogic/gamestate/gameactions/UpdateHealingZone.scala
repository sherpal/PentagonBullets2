package gamelogic.gamestate.gameactions

import gamelogic.entities.concreteentities.HealingZone
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Happens when a healing zone has to be Updated.
  */
final case class UpdateHealingZone(
    actionId: GameAction.Id,
    time: Long,
    zoneId: Entity.Id,
    ownerId: Entity.Id,
    lifeSupply: Double,
    xPos: Double,
    yPos: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(
      HealingZone(
        zoneId,
        gameState.entityByIdAs[HealingZone](zoneId).get.creationTime,
        ownerId,
        time,
        lifeSupply,
        xPos,
        yPos
      ),
      time
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
