package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

/** Sent by the players when they move around.
  */
final case class UpdatePlayerPos(
    actionId: GameAction.Id,
    time: Long,
    playerId: Entity.Id,
    newPos: Complex,
    dir: Double,
    moving: Boolean,
    rot: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    GameStateTransformer.maybe(
      gameState
        .playerById(playerId)
        .map(
          _.copy(time = time, pos = newPos, direction = dir, moving = moving, rotation = rot)
        )
        .map(WithEntity(_, time))
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
