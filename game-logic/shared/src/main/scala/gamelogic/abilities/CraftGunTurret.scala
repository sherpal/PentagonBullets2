package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.entities.concreteentities.GunTurret
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.NewGunTurret
import gamelogic.utils.IdGeneratorContainer

final case class CraftGunTurret(time: Long, useId: Ability.UseId, casterId: Entity.Id, teamId: Int, pos: Complex)
    extends Ability
    with ZeroCostAbility {

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): CraftGunTurret =
    copy(time = newTime, useId = newId)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    NewGunTurret(
      GameAction.newId(),
      time,
      Entity.newId(),
      casterId,
      teamId,
      pos,
      GunTurret.defaultRadius,
      AbilitySource
    )
  )

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

  val cooldown: Long = 30000

  val abilityId: Ability.AbilityId = Ability.craftGunTurretId

}
