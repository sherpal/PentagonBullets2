package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.gamestate.gameactions.NewBarrier
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.entities.concreteentities.Barrier
import gamelogic.utils.IdGeneratorContainer

final case class CreateBarrier(
    time: Long,
    useId: Ability.UseId,
    casterId: Entity.Id,
    teamId: Int,
    targetPos: Complex,
    rotation: Double
) extends Ability
    with ZeroCostAbility {

  val abilityId: Ability.AbilityId = Ability.createBarrierId

  val cooldown: Long = 20000

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): CreateBarrier =
    copy(time = newTime, useId = newId)

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)
    .orElse(
      Option.when(
        gameState.players.values
          .filter(_.team != teamId)
          .exists(p => p.shape.collides(p.pos, p.rotation, Barrier.shape, targetPos, rotation))
      )("There an enemy at the position you are targetting!")
    )

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] = List(
    NewBarrier(GameAction.newId(), time, Entity.newId(), casterId, teamId, targetPos, rotation, AbilitySource)
  )

}
