package gamelogic.buffs.resourcebuffs

import gamelogic.buffs.Buff.ResourceIdentifier
import gamelogic.buffs.{Buff, SimpleTickerBuff, TickerBuff}
import gamelogic.entities.Entity
import gamelogic.entities.Resource.{Energy, ResourceAmount}
import gamelogic.gamestate.gameactions.ChangeRemourceAmount
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer

/** Never ending buff which grants its bearer 10 energy every second.
  */
final case class EnergyFiller(buffId: Buff.Id, bearerId: Entity.Id, appearanceTime: Long, lastTickTime: Long)
    extends SimpleTickerBuff {

  def tickEffect(gameState: GameState, time: Long, entityIdGenerator: IdGeneratorContainer): List[GameAction] = {
    implicit def idGen: IdGeneratorContainer = entityIdGenerator
    List(
      ChangeRemourceAmount(
        GameAction.newId(),
        time,
        bearerId,
        ResourceAmount(EnergyFiller.energyRefillPerSecond, Energy)
      )
    )
  }

  val tickRate: Long = 1000L

  def changeLastTickTime(time: Long): TickerBuff = copy(lastTickTime = time)

  def duration: Long = -1L

  def resourceIdentifier: ResourceIdentifier = Buff.energyFiller

  def endingAction(gameState: GameState, time: Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): List[GameAction] = Nil
}

object EnergyFiller {

  @inline final def energyRefillPerSecond = 20.0

}
