package gamelogic.abilities

import be.doeraene.physics.Complex
import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.{GunTurret, LaserLauncher, Player}
import gamelogic.entities.ActionSource.AbilitySource
import gamelogic.gamestate.gameactions.*
import gamelogic.gamestate.{GameAction, GameState}
import be.doeraene.physics.shape.Polygon
import gamelogic.utils.IdGeneratorContainer

final case class LaserAbility(
    time: Long,
    useId: Ability.UseId,
    casterId: Entity.Id,
    teamId: Int,
    stepNumber: Int,
    pos: Complex
) extends MultiStepAbility
    with ZeroCostAbility {

  val abilityId: Ability.AbilityId = Ability.laserId

  val innerCooldown: Vector[Long] = Vector(1000, 8000)

  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction] =
    if stepNumber == 0 then
      List(NewLaserLauncher(GameAction.newId(), time, Entity.newId(), pos, casterId, AbilitySource))
    else {
      (
        gameState.players.get(casterId),
        gameState.laserLaunchers.values.find(_.ownerId == casterId)
      ) match {
        case (Some(caster), Some(laserLauncher)) =>
          val casterPos: Complex = caster.currentPosition(time)

          val directionPos     = laserLauncher.pos - casterPos
          val unitVec          = directionPos / directionPos.modulus
          val perpendicularVec = Player.radius * Complex(-unitVec.im, unitVec.re)

          val laserVertices =
            Vector(casterPos, laserLauncher.pos - perpendicularVec, laserLauncher.pos + perpendicularVec)

          val laserShape = Polygon(laserVertices, convex = true)

          DestroyLaserLauncher(GameAction.newId(), time, laserLauncher.id, AbilitySource) +:
            FireLaser(GameAction.newId(), time, casterId, laserVertices, AbilitySource) +:
            (gameState
              .allTEntities[GunTurret]
              .values
              .filterNot(_.teamId == teamId)
              .filter(turret => turret.shape.collides(turret.pos, 0, laserShape, 0, 0))
              .map(turret =>
                GunTurretTakesDamage(GameAction.newId(), time, turret.id, LaserAbility.damage, AbilitySource)
              ) ++
              gameState.players.values
                .filterNot(_.team == teamId)
                .filter(player =>
                  player.shape.collides(player.currentPosition(time), player.rotation, laserShape, 0, 0)
                )
                .map(player =>
                  PlayerTakeDamage(GameAction.newId(), time, player.id, casterId, LaserAbility.damage, AbilitySource)
                )).toList
        case _ =>
          Nil
      }

    }

  def canBeCast(gameState: GameState, time: Long): Option[String] = playerMustBeAlive(gameState, casterId)

  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): LaserAbility =
    copy(time = newTime, useId = newId)

}

object LaserAbility {

  val damage: Double = 30

  def stepFromGameState(gameState: GameState, casterId: Entity.Id): Int =
    if gameState.laserLaunchers.values.exists(_.ownerId == casterId) then 1 else 0

}
