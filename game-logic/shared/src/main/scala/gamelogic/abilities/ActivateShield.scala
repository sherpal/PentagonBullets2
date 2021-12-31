package gamelogic.abilities

import gamelogic.entities.{ActionSource, Entity, Resource}
import gamelogic.gamestate.*
import gamelogic.entities.concreteentities.Shield
import gamelogic.utils.IdGeneratorContainer
import gamelogic.gamestate.gameactions.PutSimplePassiveBuff
import gamelogic.buffs.Buff

final case class ActivateShield(time: Long, useId: Ability.UseId, playerId: Entity.Id)
    extends Ability
    with ZeroCostAbility {

  val abilityId: Ability.AbilityId = Ability.activateShieldId

  val cooldown: Long = 60000

  val casterId: Entity.Id = playerId

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    PutSimplePassiveBuff(
      GameAction.newId(),
      time,
      Shield(Buff.nextBuffId(), time, playerId),
      ActionSource.AbilitySource
    )
  )

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): ActivateShield =
    copy(time = newTime, useId = newId)

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, playerId)

}
