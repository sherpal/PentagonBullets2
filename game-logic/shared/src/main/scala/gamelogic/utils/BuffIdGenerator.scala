package gamelogic.utils

import gamelogic.buffs.Buff

import java.util.concurrent.atomic.AtomicLong

final class BuffIdGenerator(startingId: Buff.Id) extends IdGenerator[Buff.Id]:
  protected val generator: AtomicLong = new AtomicLong(startingId.toLong)

  protected def fromLong(long: Long): Buff.Id = Buff.Id(long)
