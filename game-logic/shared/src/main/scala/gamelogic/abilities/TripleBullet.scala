package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.entities.concreteentities.Bullet
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.NewBullet
import gamelogic.utils.IdGeneratorContainer

/** Fires five bullets simultaneously, in the direction of the mouse, spaced from angles -pi/16 to pi/16.
  *
  * Remark: the name does not reflect what it does anymore, but we keep for historical reasons. (Also because I'm lazy.)
  */
final case class TripleBullet(
    time: Long,
    useId: Ability.UseId,
    casterId: Entity.Id,
    teamId: Int,
    startingPos: Complex,
    rotation: Double
) extends Ability
    with ZeroCostAbility {

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): TripleBullet =
    copy(time = newTime, useId = newId)

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] =
    (0 until TripleBullet.bulletNbr)
      .map(_ * math.Pi / 8 / (TripleBullet.bulletNbr - 1) + -math.Pi / 16)
      .map(alpha =>
        NewBullet(
          GameAction.newId(),
          Entity.newId(),
          casterId,
          teamId,
          startingPos,
          Bullet.defaultRadius,
          rotation + alpha,
          Bullet.speed * 5 / 4,
          time,
          0,
          AbilitySource
        )
      )
      .toList

  val cooldown: Long = 10000

  val abilityId: Ability.AbilityId = Ability.tripleBulletId

}

object TripleBullet {

  private val bulletNbr: Int = 5

}
