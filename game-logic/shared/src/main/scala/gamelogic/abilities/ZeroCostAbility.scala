package gamelogic.abilities

import gamelogic.entities.Resource

trait ZeroCostAbility { self: Ability =>
  def cost: Resource.ResourceAmount = Resource.zero
}
