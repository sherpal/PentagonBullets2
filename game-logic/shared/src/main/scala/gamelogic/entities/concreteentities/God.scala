package gamelogic.entities.concreteentities

import gamelogic.entities.Entity

final case class God(
    id: Entity.Id,
    time: Long
) extends Entity
