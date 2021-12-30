package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.TeamFlag
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Happens when a Player manages to bring an enemy flag back to his or her side, scoring one point.
  */
final case class PlayerBringsFlagBack(
    actionId: GameAction.Id,
    time: Long,
    flagId: Entity.Id,
    playerId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  override def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    GameStateTransformer.maybe(
      gameState
        .playerById(playerId)
        .map(player => new TransformEntity[TeamFlag](flagId, time, _.withoutBearer.withNewTakenBy(player.team)))
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
