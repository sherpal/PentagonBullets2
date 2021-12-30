package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.TeamFlag
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Adds a Flag to the state. This can also be used to put the flag back to its original place, when a player drops it.
  */
final case class NewTeamFlag(
    actionId: GameAction.Id,
    time: Long,
    flagId: Entity.Id,
    teamNbr: Int,
    pos: Complex,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(TeamFlag(flagId, pos.re, pos.im, teamNbr, None, Nil), time)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
