package gamelogic.buffs.godsbuffs

import gamelogic.buffs.{Buff, SimpleTickerBuff}
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.Entity
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer
import gamelogic.entities.concreteentities.HealUnit
import gamelogic.gamestate.gameactions.NewHealUnit

final case class HealUnitDealer(buffId: Buff.Id, bearerId: Entity.Id, appearanceTime: Long, lastTickTime: Long)
    extends SimpleTickerBuff {
  def tickEffect(gameState: GameState, time: Long, idGenerator: IdGeneratorContainer): List[GameAction] = {
    implicit def gen: IdGeneratorContainer = idGenerator

    val position = HealUnit.randomPosition(gameState)

    List(NewHealUnit(GameAction.newId(), time, Entity.newId(), position, ServerSource))
  }

  val tickRate: Long = 5000

  def duration: Long = -1

  def changeLastTickTime(time: Long): HealUnitDealer = copy(lastTickTime = time)

  def resourceIdentifier: Buff.ResourceIdentifier = Buff.healUnitDealer

  def endingAction(gameState: GameState, time: Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): List[GameAction] = List.empty
}
