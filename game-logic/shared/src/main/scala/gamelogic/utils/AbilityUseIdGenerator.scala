package gamelogic.utils

import java.util.concurrent.atomic.AtomicLong
import gamelogic.abilities.Ability
import gamelogic.abilities.Ability.UseId

final class AbilityUseIdGenerator(startingId: Ability.UseId) extends IdGenerator[Ability.UseId] {
  protected val generator: AtomicLong = new AtomicLong(startingId.toLong)

  protected def fromLong(long: Long): UseId = Ability.UseId(long)
}

object AbilityUseIdGenerator {
  implicit def fromContainer(implicit idGeneratorContainer: IdGeneratorContainer): AbilityUseIdGenerator =
    idGeneratorContainer.abilityUseIdGenerator
}
