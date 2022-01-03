package gamelogic.abilities

import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.gamestate.gameactions.PutSimplePassiveBuff
import gamelogic.buffs.Buff
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.entities.concreteentities.BulletGlue
import gamelogic.utils.IdGeneratorContainer

final case class PutBulletGlue(time: Long, useId: Ability.UseId, casterId: Entity.Id, teamId: Int)
    extends Ability
    with ZeroCostAbility {

  val abilityId: Ability.AbilityId = Ability.putBulletGlueId

  val cooldown: Long = 20000

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): PutBulletGlue =
    copy(time = newTime, useId = newId)

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    PutSimplePassiveBuff(GameAction.newId(), time, BulletGlue(Buff.nextBuffId(), time, casterId, teamId), AbilitySource)
  )

}
