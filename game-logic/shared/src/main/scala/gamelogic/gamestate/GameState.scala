package gamelogic.gamestate

import be.doeraene.physics.Complex
import gamelogic.entities.*
import gamelogic.buffs.*
import gamelogic.entities.concreteentities.{GameArea, HealingZone, *}
import be.doeraene.physics.shape.{Polygon, Shape}
import entitiescollections.PlayerTeam

import scala.annotation.tailrec
import scala.reflect.ClassTag

object GameState {

  def initialGameState: GameState = new GameState(
    0L,
    None,
    None,
    Shape.regularPolygon(3),
    Map.empty,
    Map.empty,
    Map.empty,
    Map.empty,
    Map.empty
  )

}

final class GameState(
    val time: Long,
    val startTime: Option[Long],
    val endTime: Option[Long],
    val gameBounds: Polygon,
    val castingEntityInfo: Map[Entity.Id, EntityCastingInfo],
    val passiveBuffs: Map[Entity.Id, Map[Buff.Id, PassiveBuff]],
    val tickerBuffs: Map[Entity.Id, Map[Buff.Id, TickerBuff]],
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
      tickerBuffs: Map[Entity.Id, Map[Buff.Id, TickerBuff]] = tickerBuffs,
      entities: Map[Entity.Id, Entity] = entities,
      deadPlayers: Map[Entity.Id, Player] = deadPlayers
  ): GameState = GameState(
    time max newTime,
    startTime,
    endTime,
    gameBounds,
    castingEntityInfo,
    passiveBuffs,
    tickerBuffs,
    entities,
    deadPlayers
  )

  def started: Boolean = startTime.isDefined
  def ended: Boolean   = endTime.isDefined

  def isPlaying: Boolean = started && !ended

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

  /** Returns whether that [[Entity.Id]] exists as is of this particular type of [[Entity]]. */
  def entityIdExistsAs[T <: Entity](entityId: Entity.Id)(using ClassTag[T]): Boolean =
    entityByIdAs[T](entityId).isDefined

  /** Creates a Map from entity id to the corresponding entity, but only for those of type `T`.
    */
  def allTEntities[T <: Entity](using ClassTag[T]): Map[Entity.Id, T] =
    entities.collect { case (id, entity: T) => (id, entity) }

  @inline def abilityGivers: Map[Entity.Id, AbilityGiver]   = allTEntities[AbilityGiver]
  @inline def barriers: Map[Entity.Id, Barrier]             = allTEntities[Barrier]
  @inline def bullets: Map[Entity.Id, Bullet]               = allTEntities[Bullet]
  @inline def damageZones: Map[Entity.Id, DamageZone]       = allTEntities[DamageZone]
  @inline def healUnits: Map[Entity.Id, HealUnit]           = allTEntities[HealUnit]
  @inline def laserLaunchers: Map[Entity.Id, LaserLauncher] = allTEntities[LaserLauncher]
  @inline def mists: Map[Entity.Id, Mist]                   = allTEntities[Mist]
  @inline def players: Map[Entity.Id, Player]               = allTEntities[Player]

  def playerById(playerId: Entity.Id): Option[Player] = entityByIdAs[Player](playerId)

  def teamsByPlayerId: Map[Entity.Id, PlayerTeam] = (players ++ deadPlayers).values
    .groupBy(_.team)
    .flatMap { (teamId, players) =>
      val playerTeam = new PlayerTeam(teamId, players.map(_.id).toList)
      players.map(_.id -> playerTeam)
    }
    .toMap

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

  def withDeadPlayer(time: Long, player: Player): GameState =
    copy(deadPlayers = deadPlayers + (player.id -> player), newTime = time)

  def collidingPlayerObstacles(player: Player): Iterable[Body] = collidingPlayerObstacles(player.team)

  def collidingPlayerObstacles(playerTeam: Int): Iterable[Body] =
    entities.values.collect {
      case barrier: Barrier if barrier.teamId != playerTeam => barrier
      case obstacle: Obstacle                               => obstacle
    }

  def allTPassiveBuffs[T <: PassiveBuff](using ClassTag[T]): Iterable[T] =
    passiveBuffs.values.flatMap(_.values.collect { case t: T => t })

  def passiveBuffsById: Map[Buff.Id, PassiveBuff] = passiveBuffs.values.flatten.toMap

  def passiveTBuffsById[T <: PassiveBuff](using ClassTag[T]): Map[Buff.Id, T] =
    passiveBuffsById.collect { case (id, t: T) => (id, t) }

  /** Returns whether the entity with given [[Entity.Id]] is currently casting. */
  def entityIsCasting(entityId: Entity.Id): Boolean = entityIsCasting(entityId, 0L)
  def entityIsCasting(entityId: Entity.Id, delay: Long): Boolean =
    castingEntityInfo.get(entityId).fold(false) { castingInfo =>
      time + delay - castingInfo.startedTime <= castingInfo.castingTime
    }

  lazy val gameArea: GameArea = GameArea(players.size + deadPlayers.size)

  def gameAreaSideLength: Int = gameArea.gameAreaSideLength

  def findPositionInMist(minDist: Int = 100, maxAttempt: Int = 50): Option[Complex] = {
    val posGenerator: () => Complex =
      allTEntities[Mist].values.headOption.fold(() => gameArea.randomComplexPos())(mist =>
        () => gameArea.randomComplexPos(mist)
      )
    val playersNow = players.values
    LazyList
      .from(1)
      .take(maxAttempt)
      .map(_ => posGenerator())
      .find(pos => playersNow.forall(_.pos.distanceTo(pos) > minDist))
  }

}
