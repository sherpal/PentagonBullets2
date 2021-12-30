package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.{HealingZone, Player}
import gamelogic.gamestate.statetransformers.{GameStateTransformer, TransformEntity, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

/** Happens when a [[entities.HealingZone]] heals someone.
  */
final case class HealingZoneHeals(
    actionId: GameAction.Id,
    time: Long,
    healedUnitId: Entity.Id,
    healingZoneId: Entity.Id,
    amount: Double,
    actionSource: ActionSource
) extends GameAction {

  override def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    GameStateTransformer.maybe(
      gameState.playerById(healedUnitId).map { healedPlayer =>
        GameStateTransformer.when(_ => healedPlayer.lifeTotal < Player.maxLife)(
          WithEntity(
            healedPlayer.copy(lifeTotal = math.min(Player.maxLife, healedPlayer.lifeTotal + amount)),
            time
          ) ++ TransformEntity[HealingZone](healingZoneId, time, _.subtractLifeSupply(amount))
        )
      }
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
