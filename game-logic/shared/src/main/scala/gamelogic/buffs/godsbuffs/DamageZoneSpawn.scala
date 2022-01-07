package gamelogic.buffs.godsbuffs

import be.doeraene.physics.Complex
import gamelogic.buffs.{Buff, SimpleTickerBuff}
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.gameactions.*
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer

import scala.util.Random

final case class DamageZoneSpawn(buffId: Buff.Id, bearerId: Entity.Id, appearanceTime: Long, lastTickTime: Long)
    extends SimpleTickerBuff {

  def tickEffect(gameState: GameState, time: Long, idGenerator: IdGeneratorContainer): List[GameAction] = {
    implicit def gen: IdGeneratorContainer = idGenerator

    gameState
      .findPositionInMist()
      .toList
      .map(position =>
        UpdateDamageZone(
          GameAction.newId(),
          time,
          Entity.newId(),
          time,
          time,
          position,
          DamageZone.startingRadius,
          ServerSource
        )
      )
  }

  val tickRate: Long = DamageZone.popRate

  def duration: Long = -1

  def changeLastTickTime(time: Long): DamageZoneSpawn = copy(lastTickTime = time)

  def resourceIdentifier: Buff.ResourceIdentifier = Buff.damageZoneSpawn

  def endingAction(gameState: GameState, time: Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): List[GameAction] = List.empty
}
