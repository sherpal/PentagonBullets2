package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.{HealUnit, Player}
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Happens when a player goes onto a HealUnit
  */
final case class PlayerTakeHealUnit(
    actionId: GameAction.Id,
    time: Long,
    playerId: Entity.Id,
    healUnitId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    RemoveEntity(healUnitId, time) ++ TransformEntity[Player](playerId, time, HealUnit.playerTakeUnit(_, time))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
