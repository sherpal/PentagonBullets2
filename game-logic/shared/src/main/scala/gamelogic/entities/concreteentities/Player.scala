package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import gamelogic.abilities.Ability
import be.doeraene.physics.shape.{Polygon, Shape}
import gamelogic.abilities.Ability.UseId
import gamelogic.entities.*
import gamelogic.entities.Entity.TeamId
import gamelogic.entities.WithPosition.Angle
import gamelogic.entities.Resource.ResourceAmount
import utils.misc.RGBColour

/** A Player represents a Human player.
  */
final case class Player(
    id: Entity.Id,
    team: TeamId,
    time: Long,
    name: String,
    pos: Complex = 0,
    direction: Double = 0,
    speed: Double = Player.speed,
    moving: Boolean = false,
    rotation: Double = 0,
    shape: Polygon = Player.shape,
    lifeTotal: Double = 100,
    allowedAbilities: List[Ability.AbilityId],
    relevantUsedAbilities: Map[Ability.UseId, Ability],
    energy: Double,
    colour: RGBColour
) extends MovingBody
    with Living
    with WithAbilities {

  def maxResourceAmount: Double = 100

  def resourceAmount: ResourceAmount = ResourceAmount(energy, Resource.Energy)

  def patchResourceAmount(newResourceAmount: ResourceAmount): Player =
    copy(energy = newResourceAmount.amount)

  def takeDamage(amount: Double): Player = copy(lifeTotal = lifeTotal - amount)

  def useAbility(ability: Ability): Player = copy(
    time = ability.time,
    relevantUsedAbilities = relevantUsedAbilities + (ability.useId -> ability)
  )

  def removeRelevantAbility(useId: UseId, time: Long): Player = copy(
    time = time,
    relevantUsedAbilities = relevantUsedAbilities - useId
  )

  def move(time: Long, position: Complex, direction: Angle, rotation: Angle, speed: Double, moving: Boolean): Player =
    copy(
      time = time,
      pos = position,
      moving = moving,
      direction = direction,
      rotation = rotation
    )

  def addAllowedAbility(abilityId: Ability.AbilityId): Player =
    copy(allowedAbilities = allowedAbilities :+ abilityId)
}

object Player {
  val radius: Double = 20

  val speed: Double = 200

  val shape: Polygon = Shape.regularPolygon(5, radius)

  val maxLife: Double = 100

  val maxBulletRate: Long = 100

  val maxEnergy: Double = 100

  def bulletHitPlayer(player: Player, damage: Double, time: Long): Player = player.takeDamage(damage)

  def smashBulletHitPlayer(player: Player, time: Long): Player =
    player.copy(lifeTotal = math.ceil(player.lifeTotal / 2))

}
