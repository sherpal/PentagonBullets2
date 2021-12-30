package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.Circle
import gamelogic.entities.{Body, Entity}
import gamelogic.abilities.Ability

/** When a [[Player]] catches an AbilityGiver, it learns the ability whose ID is abilityId. If it already has the
  * ability, the cooldown will be divided by 2.
  */
final case class AbilityGiver(id: Entity.Id, time: Long, xPos: Double, yPos: Double, abilityId: Ability.AbilityId)
    extends Body {

  def pos: Complex = Complex(xPos, yPos)

  val rotation: Double = 0.0

  val shape: Circle = Circle(AbilityGiver.radius)

}

object AbilityGiver {

  val radius: Int = 20

  def playerTakeAbilityGiver(player: Player, time: Long, abilityId: Ability.AbilityId): Player =
    player.addAllowedAbility(abilityId)

}
