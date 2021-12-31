package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.NewSmashBullet
import gamelogic.utils.IdGeneratorContainer
import gamelogic.entities.concreteentities.SmashBullet

final case class LaunchSmashBullet(
    time: Long,
    useId: Ability.UseId,
    casterId: Entity.Id,
    startingPos: Complex,
    rotation: Double
) extends Ability
    with ZeroCostAbility {

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): LaunchSmashBullet =
    copy(time = newTime, useId = newId)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    NewSmashBullet(
      GameAction.newId(),
      time,
      Entity.newId(),
      casterId,
      startingPos,
      rotation,
      SmashBullet.defaultRadius,
      SmashBullet.speed,
      AbilitySource
    )
  )

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

  val cooldown: Long = 15000

  val abilityId: Ability.AbilityId = Ability.launchSmashBulletId

}
