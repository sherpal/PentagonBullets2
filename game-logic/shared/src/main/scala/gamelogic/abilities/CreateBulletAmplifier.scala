package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.NewBulletAmplifier
import gamelogic.utils.IdGeneratorContainer

/** Create a [[entities.BulletAmplifier]] for the caster's team.
  */
final case class CreateBulletAmplifier(
    time: Long,
    useId: Ability.UseId,
    casterId: Entity.Id,
    targetPos: Complex,
    rotation: Double
) extends Ability
    with ZeroCostAbility {

  val abilityId: Ability.AbilityId = Ability.createBulletAmplifierId

  val cooldown: Long = 15000

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): CreateBulletAmplifier =
    copy(time = newTime, useId = newId)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    NewBulletAmplifier(GameAction.newId(), time, Entity.newId(), casterId, rotation, targetPos, AbilitySource)
  )

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

}
