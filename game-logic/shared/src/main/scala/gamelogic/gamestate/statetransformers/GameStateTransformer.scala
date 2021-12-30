package gamelogic.gamestate.statetransformers

import cats.kernel.Monoid
import gamelogic.gamestate.GameState

/** A [[GameStateTransformer]] is a concrete materialization of a [[gamelogic.gamestate.GameAction]].
  *
  * Typically, a [[gamelogic.gamestate.GameAction]] generates one or more [[GameStateTransformer]] that concretely act
  * on the [[gamelogic.gamestate.GameState]]. This action transformers don't need to be serialized through communication
  * and can hence just be simple classes, involving other non-serializable objects.
  *
  * Important: [[GameStateTransformer]]s are not supposed to check whether their action make sense, but are merely
  * building blocks of the current [[gamelogic.gamestate.GameState]].
  */
trait GameStateTransformer {
  final def ++(that: GameStateTransformer): GameStateTransformer = GameStateTransformer.Compose(this, that)

  def apply(gameState: GameState): GameState
}

object GameStateTransformer {
  def identityTransformer: GameStateTransformer = (gs: GameState) => gs

  /** Applies the optional [[GameStateTransformer]] when it's defined */
  def maybe(maybeTransformer: Option[GameStateTransformer]): GameStateTransformer = Optional(maybeTransformer)

  /** Applies the given transformer when the condition is met. */
  def when(predicate: GameState => Boolean)(transformer: GameStateTransformer): GameStateTransformer =
    Conditional(predicate, transformer)

  /** Applies the given transformer when the condition is *not* met. */
  def unless(predicate: GameState => Boolean)(transformer: GameStateTransformer): GameStateTransformer =
    Conditional(!predicate(_), transformer)

  def fromOption[T](maybeT: GameState => Option[T])(transformer: T => GameStateTransformer): GameStateTransformer =
    FromOptionalOutput(maybeT, transformer)

  final class Compose(left: GameStateTransformer, right: GameStateTransformer) extends GameStateTransformer:
    def apply(gameState: GameState): GameState = right(left(gameState))

  final class Optional(maybeTransformer: Option[GameStateTransformer]) extends GameStateTransformer {
    def apply(gameState: GameState): GameState = maybeTransformer.fold(gameState)(_(gameState))
  }

  final class Conditional(predicate: GameState => Boolean, transformer: GameStateTransformer)
      extends GameStateTransformer {
    def apply(gameState: GameState): GameState =
      if predicate(gameState) then transformer(gameState) else gameState
  }

  final class FromOptionalOutput[T](maybeOutput: GameState => Option[T], transformer: T => GameStateTransformer)
      extends GameStateTransformer {
    override def apply(gameState: GameState): GameState =
      maybeOutput(gameState).fold(gameState)(transformer(_).apply(gameState))
  }

  implicit val monoid: Monoid[GameStateTransformer] = new Monoid[GameStateTransformer] {
    def empty: GameStateTransformer = identityTransformer

    def combine(x: GameStateTransformer, y: GameStateTransformer): GameStateTransformer = x ++ y
  }

}
