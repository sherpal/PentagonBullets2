package gamelogic.abilities

import gamelogic.entities.Resource.ResourceAmount
import gamelogic.entities.{Entity, Resource, WithAbilities}
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.{AbilityUseIdGenerator, IdGeneratorContainer}
import io.circe.{Decoder, Encoder, Json}

import scala.annotation.tailrec

/** A [[gamelogic.abilities.Ability]] represents an action that an entity can take besides moving.
  *
  * Each "class" has its own set of abilities that they can use. The abilities of a given class are implemented in a
  * dedicated package that has the name of the class. Abilities in two different classes can be very similar, basically
  * only changing the constants of the effect.
  *
  * Each ability has a unique id that is given in the code itself.
  *
  * Note: if an ability is though to have no cooldown nor casting time, it is best to set the cooldown to a minimal
  * amount, in order to have a GCD. perhaps in the future, this should be handled automatically.
  */
trait Ability {

  /** Unique id given manually to each ability. */
  def abilityId: Ability.AbilityId

  /** Id created for each use. */
  def useId: Ability.UseId

  /** Duration (in millis) before this ability */
  def cooldown: Long

  /** Id of the entity that cast the spell.
    */
  def casterId: Entity.Id

  /** Game Time (in millis) at which the ability's casting is complete. */
  def time: Long

  /** Cost of the ability. */
  def cost: ResourceAmount

  def castingTime: Long = 0L

  /** Type of resource needed to use the ability. */
  final def resource: Resource = cost.resourceType

  /** Generates all actions that this ability generates when completed. These actions may depend on the
    * [[gamelogic.gamestate.GameState]] at the time the ability is completed.
    */
  def createActions(
      gameState: GameState
  )(using IdGeneratorContainer): List[GameAction]

  /** Change the time and id of this ability, without changing the rest. */
  def copyWithNewTimeAndId(newTime: Long, newId: Ability.UseId): Ability

  /** Returns None when the ability can indeed be cast, otherwise return Some(error message).
    *
    * @param gameState
    *   state of the game at the time the entity wants to use the ability
    * @param time
    *   time at which the entity wants to use the ability
    * @return
    *   Some error message when the ability can't be cast at that time, with that [[GameState]].
    */
  def canBeCast(gameState: GameState, time: Long): Option[String]

  def playerMustBeAlive(gameState: GameState, playerId: Entity.Id): Option[String] =
    gameState.playerById(playerId).toLeft(s"Player with id $playerId is not alive").toOption

  def isUp(caster: WithAbilities, now: Long, allowedError: Long = 0): Boolean =
    caster.relevantUsedAbilities.values
      .filter(_.abilityId == abilityId)
      .forall(ability =>
        (now - ability.time + allowedError) >= cooldown / caster.allowedAbilities.count(_ == abilityId)
      )

  protected def canBeCastAll(gameState: GameState, time: Long)(
      checks: (GameState, Long) => Option[String]*
  ): Option[String] = {
    @tailrec
    def canBeCastAllList(remainingChecks: List[(GameState, Long) => Option[String]]): Option[String] =
      remainingChecks match {
        case Nil => None
        case head :: tail =>
          head(gameState, time) match {
            case Some(value) => Some(value)
            case None        => canBeCastAllList(tail)
          }
      }

    canBeCastAllList(checks.toList)
  }

  /** Returns whether this ability can be cast at that time with this [[GameState]].
    */
  final def canBeCastBoolean(gameState: GameState, time: Long): Boolean =
    canBeCast(gameState, time).isEmpty

  /** Alias for [[canBeCastBoolean]] */
  @inline final def isLegal(gameState: GameState, time: Long): Boolean = canBeCastBoolean(gameState, time)

}

object Ability {

  opaque type UseId = Long

  object UseId {
    implicit final class UserIdExtension(useId: UseId) {
      @inline def toLong: Long = useId: Long
    }

    def apply(long: Long): UseId = long

    def initial: UseId = 0L
  }

  def nextUseId()(implicit useIdGenerator: AbilityUseIdGenerator): UseId = useIdGenerator.nextId()

  opaque type AbilityId = Int

  object AbilityId {
    implicit final class AbilityIdExtension(abilityId: AbilityId) {
      @inline def toInt: Int = abilityId: Int
    }

    def fromInt(intId: Int): Option[AbilityId] = Option.when(intId <= lastAbilityId && 0 <= intId)(intId)
  }
  private var lastAbilityId: AbilityId                       = 0
  def maxAbilityId: AbilityId                                = lastAbilityId
  private def nextAbilityId[A <: Ability](): AbilityIdFor[A] = { lastAbilityId += 1; lastAbilityId }

  type AbilityIdFor[A <: Ability] = AbilityId

  def abilityIdCount: AbilityId = lastAbilityId

  val activateShieldId: AbilityIdFor[ActivateShield]               = nextAbilityId()
  val bigBulletId: AbilityIdFor[BigBullet]                         = nextAbilityId()
  val craftGunTurretId: AbilityIdFor[CraftGunTurret]               = nextAbilityId()
  val createBarrierId: AbilityIdFor[CreateBarrier]                 = nextAbilityId()
  val createBulletAmplifierId: AbilityIdFor[CreateBulletAmplifier] = nextAbilityId()
  val createHealingZoneId: AbilityIdFor[CreateHealingZone]         = nextAbilityId()
  val laserId: AbilityIdFor[LaserAbility]                          = nextAbilityId()
  val launchSmashBulletId: AbilityIdFor[LaunchSmashBullet]         = nextAbilityId()
  val putBulletGlueId: AbilityIdFor[PutBulletGlue]                 = nextAbilityId()
  val teleportationId: AbilityIdFor[Teleportation]                 = nextAbilityId()
  val tripleBulletId: AbilityIdFor[TripleBullet]                   = nextAbilityId()

  val allAbilityIds: Vector[AbilityId] = (1 to maxAbilityId).toVector

  def nonShieldAbilityIds: List[AbilityId] = allAbilityIds.toList.filterNot(_ == activateShieldId)

}
