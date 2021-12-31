package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.NewBullet
import gamelogic.utils.IdGeneratorContainer
import gamelogic.entities.concreteentities.Bullet

/** Launch a BigBullet that is three times as big, and deals three times as much damage.
  */
final case class BigBullet(
    time: Long,
    useId: Ability.UseId,
    casterId: Entity.Id,
    teamId: Int,
    startingPos: Complex,
    rotation: Double
) extends Ability
    with ZeroCostAbility {

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): BigBullet =
    copy(time = newTime, useId = newId)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    NewBullet(
      GameAction.newId(),
      Entity.newId(),
      casterId,
      teamId,
      startingPos,
      3 * Bullet.defaultRadius,
      rotation,
      Bullet.speed * 2,
      time,
      0,
      AbilitySource
    )
  )

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

  val cooldown: Long = 10000

  val abilityId: Ability.AbilityId = Ability.bigBulletId

}
