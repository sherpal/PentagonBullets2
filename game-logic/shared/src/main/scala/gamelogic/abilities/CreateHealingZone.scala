package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.entities.concreteentities.HealingZone
import gamelogic.gamestate.gameactions.NewHealingZone
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer

/** Puts a HealingZone for the team at the target position
  */
final case class CreateHealingZone(time: Long, useId: Ability.UseId, casterId: Entity.Id, targetPos: Complex)
    extends Ability
    with ZeroCostAbility {

  val abilityId: Ability.AbilityId = Ability.createHealingZoneId

  val cooldown: Long = 30000

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): CreateHealingZone =
    copy(time = newTime, useId = newId)

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    NewHealingZone(
      GameAction.newId(),
      time,
      Entity.newId(),
      casterId,
      HealingZone.lifeSupply,
      targetPos.re,
      targetPos.im,
      AbilitySource
    )
  )

}
