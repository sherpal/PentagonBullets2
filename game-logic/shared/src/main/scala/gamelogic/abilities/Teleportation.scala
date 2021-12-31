package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.entities.Entity
import gamelogic.gamestate.gameactions.TranslatePlayer
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer

/** The Unit goes instantly to the endPos position.
  */
final case class Teleportation(
    time: Long,
    useId: Ability.UseId,
    casterId: Entity.Id,
    startingPos: Complex,
    endPos: Complex
) extends Ability
    with ZeroCostAbility {

  val abilityId: Ability.AbilityId = Ability.teleportationId

  val cooldown: Long = 20000

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): Teleportation =
    copy(time = newTime, useId = newId)

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)
    .orElse(gameState.playerById(casterId).flatMap { entity =>
      Option
        .when(
          gameState
            .collidingPlayerObstacles(entity)
            .exists(obs => obs.shape.collides(obs.pos, 0, entity.shape, endPos, entity.rotation))
        )(
          "Target position makes you collide with obstacle!"
        )
        .orElse(
          Option.unless(
            gameState.gameBounds.collides(Complex(0, 0), 0, entity.shape, endPos, entity.rotation)
          )("Target position must be within game bounds!")
        )
    })

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] =
    List(TranslatePlayer(GameAction.newId(), time, casterId, endPos, AbilitySource))

}
