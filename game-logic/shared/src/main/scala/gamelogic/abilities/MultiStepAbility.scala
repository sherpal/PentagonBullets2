package gamelogic.abilities

import gamelogic.entities.WithAbilities

/** A MultiStepAbility is a specification that can have [[Ability]]'s specifying that the ability occurs in multiple
  * times.
  *
  * A MultiStepAbility has inner cooldown between each step.
  *
  * Example: The laser. First time the ability is used, is pops the laser source on the ground. Then, when the ability
  * is used, the laser is activated.
  */
trait MultiStepAbility extends Ability {

  val stepNumber: Int

  private def previousStepNumber: Int = (if stepNumber == 0 then innerCooldown.length else stepNumber) - 1

  /** Cooldown between the steps. innerCooldown(n) is the cooldown from step n to step n + 1 (mod number of steps) */
  val innerCooldown: Vector[Long]

  def cooldown: Long = innerCooldown(stepNumber)

  override def isUp(caster: WithAbilities, now: Long, allowedError: Long = 0): Boolean =
    caster.relevantUsedAbilities.values
      .filter(_.abilityId == abilityId)
      .forall(ability =>
        (now - ability.time + allowedError) >= innerCooldown(previousStepNumber) / caster.allowedAbilities
          .count(_ == abilityId)
      )

}
