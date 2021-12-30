package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.Player
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class PlayerHitByMultipleBullets(
    actionId: GameAction.Id,
    time: Long,
    bulletIds: List[Entity.Id],
    playerId: Entity.Id,
    totalDamage: Double,
    actionSource: ActionSource
) extends GameAction {

  private def allBulletsRemoved = GameStateTransformer.monoid.combineAll(bulletIds.map(new RemoveEntity(_, time)))

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    TransformEntity[Player](playerId, time, Player.bulletHitPlayer(_, totalDamage, time)) ++ allBulletsRemoved

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
