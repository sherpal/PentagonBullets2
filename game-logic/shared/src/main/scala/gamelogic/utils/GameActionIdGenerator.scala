package gamelogic.utils

import java.util.concurrent.atomic.AtomicLong
import gamelogic.gamestate.GameAction
import gamelogic.gamestate.GameAction.Id

final class GameActionIdGenerator(startingId: GameAction.Id) extends IdGenerator[GameAction.Id] {
  protected val generator: AtomicLong = new AtomicLong(startingId.toLong)

  protected def fromLong(long: Long): Id = GameAction.Id(long)
}

object GameActionIdGenerator {
  implicit def fromContainer(implicit idGeneratorContainer: IdGeneratorContainer): GameActionIdGenerator =
    idGeneratorContainer.gameActionIdGenerator
}
