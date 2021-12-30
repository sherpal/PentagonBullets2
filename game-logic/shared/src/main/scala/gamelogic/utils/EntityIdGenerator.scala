package gamelogic.utils

import java.util.concurrent.atomic.AtomicLong

import gamelogic.entities.Entity

final class EntityIdGenerator(startingId: Entity.Id) extends IdGenerator[Entity.Id] {
  protected val generator = new AtomicLong(startingId.toLong)

  protected def fromLong(long: Long): Entity.Id = Entity.Id(long)
}
