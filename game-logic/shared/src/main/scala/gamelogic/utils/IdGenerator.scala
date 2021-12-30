package gamelogic.utils

import java.util.concurrent.atomic.AtomicLong

trait IdGenerator[Id] {

  protected val generator: AtomicLong

  protected def fromLong(long: Long): Id

  @inline def nextId(): Id = fromLong(generator.getAndIncrement())

  @inline def apply(): Id = nextId()

  def currentValue: Id = fromLong(generator.get())

}
