package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class PlayerHitBySmashBullet(
    actionId: GameAction.Id,
    time: Long,
    playerId: Entity.Id,
    bulletId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[Player](playerId, time, Player.smashBulletHitPlayer(_, time))

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
