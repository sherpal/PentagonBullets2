package gamelogic.gamestate

import gamelogic.entities.*
import gamelogic.buffs.*
import gamelogic.entities.concreteentities.{HealingZone, *}
import be.doeraene.physics.shape.Polygon

import scala.reflect.ClassTag

final class GameState(
    val time: Long,
    val startTime: Option[Long],
    val endTime: Option[Long],
    val gameBounds: Polygon,
    val castingEntityInfo: Map[Entity.Id, EntityCastingInfo],
    val passiveBuffs: Map[Entity.Id, Map[Buff.Id, PassiveBuff]],
    //val tickerBuffs: Map[Entity.Id, Map[Buff.Id, TickerBuff]],
    val entities: Map[Entity.Id, Entity],
    val deadPlayers: Map[Entity.Id, Player]
) {

  def copy(
      newTime: Long = time,
      startTime: Option[Long] = startTime,
      endTime: Option[Long] = endTime,
      gameBounds: Polygon = gameBounds,
      castingEntityInfo: Map[Entity.Id, EntityCastingInfo] = castingEntityInfo,
      passiveBuffs: Map[Entity.Id, Map[Buff.Id, PassiveBuff]] = passiveBuffs,
//            tickerBuffs: Map[Entity.Id, Map[Buff.Id, TickerBuff]]   = tickerBuffs,
      entities: Map[Entity.Id, Entity] = entities,
      deadPlayers: Map[Entity.Id, Player] = deadPlayers
  ): GameState = GameState(
    time max newTime,
    startTime,
    endTime,
    gameBounds,
    castingEntityInfo,
    passiveBuffs,
//    tickerBuffs,
    entities,
    deadPlayers
  )

  def started: Boolean = startTime.isDefined
  def ended: Boolean   = endTime.isDefined

  def starts(time: Long, bounds: Polygon): GameState = copy(newTime = time, startTime = Some(time), gameBounds = bounds)

  def ends(time: Long): GameState = copy(newTime = time, endTime = Some(time))

  /** Applies the effects of all the actions in the list to this [[GameState]]. Actions are assumed to be ordered in
    * time already.
    */
  def applyActions(actions: List[GameAction]): GameState = actions.foldLeft(this) { (currentGameState, nextAction) =>
    nextAction(currentGameState)
  }

  def isLegalAction(action: GameAction): Boolean = action.isLegal(gameState = this).isEmpty

  /** Applies the effects of all the current passive buffs to the given actions.
    *
    * Each passive buff takes an action and returns a list of actions caused by the changed. We apply all changes to all
    * cumulative actions that happen.
    *
    * This has one important consequence: it is not commutative on the set of actions. This *should* not be an issue,
    * because it should be in the contract of changer that they should not violate commutativity. Nonetheless, this is
    * something to keep in mind for the future. Perhaps a passive buff could also have a priority.
    */
  def applyActionChangers(action: GameAction): List[GameAction] =
    passiveBuffs.valuesIterator
      .flatMap(_.valuesIterator)
      .map(buff => buff.actionTransformer(_))
      .foldLeft(List(action))(_.flatMap(_))

  /** See other overloaded methods. */
  def applyActionChangers(actions: List[GameAction]): List[GameAction] = {
    val changers = passiveBuffs.valuesIterator.flatMap(_.valuesIterator).toList
    actions.flatMap(action =>
      changers.foldLeft(List(action)) { (as: List[GameAction], changer: PassiveBuff) =>
        as.flatMap(changer.actionTransformer)
      }
    )
  }

  /** Creates a partial function which filters all [[gamelogic.entities.Entity]] of the specified type `T`.
    */
  def filterT[T <: Entity](using ClassTag[T]): PartialFunction[Entity, T] = { case entity: T => entity }

  /** Returns the entity with the given Id if it exists and if it is of type `T`. Returns None otherwise.
    */
  def entityByIdAs[T <: Entity](entityId: Entity.Id)(using ClassTag[T]): Option[T] =
    entities.get(entityId).collect(filterT[T])

  /** Creates a Map from entity id to the corresponding entity, but only for those of type `T`.
    */
  def allTEntities[T <: Entity](using ClassTag[T]): Map[Entity.Id, T] =
    entities.collect { case (id, entity: T) => (id, entity) }

  def players: Map[Entity.Id, Player] = allTEntities[Player]

  def playerById(playerId: Entity.Id): Option[Player] = entityByIdAs[Player](playerId)

  def isPlayerAlive(playerId: Entity.Id): Boolean = entityByIdAs[Player](playerId).isDefined

  def obstacles: Map[Entity.Id, Obstacle] = allTEntities[Obstacle]

  def removeEntityById(id: Entity.Id, time: Long): GameState =
    copy(newTime = time, entities = entities - id)

  def withEntity[T <: Entity](entityId: Entity.Id, time: Long, entity: T): GameState =
    copy(newTime = time, entities = entities + (entityId -> entity))

  def removePlayer(playerId: Entity.Id, time: Long): GameState = removeEntityById(playerId, time)

  def removeDeadPlayer(playerId: Entity.Id, time: Long): GameState = copy(
    deadPlayers = deadPlayers - playerId,
    newTime = time
  )

  def withAbilityGiver(abilityGiverId: Entity.Id, time: Long, abilityGiver: AbilityGiver): GameState = ???

  def withBullet(id: Entity.Id, time: Long, bullet: Bullet): GameState = withEntity(id, time, bullet)

  def withBarrier(id: Entity.Id, time: Long, barrier: Barrier): GameState = withEntity(id, time, barrier)

  def withBulletAmplifier(id: Entity.Id, time: Long, bulletAmplifier: BulletAmplifier): GameState =
    withEntity(id, time, bulletAmplifier)

  def withDeadPlayer(time: Long, player: Player): GameState =
    copy(deadPlayers = deadPlayers + (player.id -> player), newTime = time)

  def withGunTurret(turretId: Entity.Id, time: Long, gunTurret: GunTurret): GameState =
    withEntity(turretId, time, gunTurret)

  def withHealUnit(id: Entity.Id, time: Long, healUnit: HealUnit): GameState = withEntity(id, time, healUnit)

  def withHealingZone(zoneId: Entity.Id, time: Long, zone: HealingZone): GameState = withEntity(zoneId, time, zone)

  def withPlayer(playerId: Entity.Id, time: Long, player: Player): GameState = withEntity(playerId, time, player)

}
