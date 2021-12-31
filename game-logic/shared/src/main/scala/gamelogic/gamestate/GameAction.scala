package gamelogic.gamestate

import boopickle.Default.*
import gamelogic.entities.Entity
import gamelogic.utils.IdGeneratorContainer
//import gamelogic.gamestate.gameactions._
import gamelogic.gamestate.statetransformers.GameStateTransformer
import io.circe.{Decoder, Encoder, Json}
import math.Ordered.orderingToOrdered

trait GameAction extends Ordered[GameAction] {

  val actionId: GameAction.Id

  /** Time at which the action occurred (in millis) */
  val time: Long

  /** Describes how this action affects a given GameState.
    *
    * This is done by first applying all transformation from [[gamelogic.buffs.PassiveBuff]] to this action, then
    * folding over all created actions.
    *
    * We can't use monoid aggregation here otherwise all action transformers are created with the first game state and
    * not the folded ones.
    */
  final def apply(gameState: GameState): GameState =
    gameState.applyActionChangers(this).foldLeft(gameState) { (currentGameState, nextAction) =>
      nextAction.createAndApplyGameStateTransformer(currentGameState)
    }

  /** Creates the [[gamelogic.gamestate.statetransformers.GameStateTransformer]] that will effectively affect the game.
    * If more than one building block must be used, you can compose them using their `++` method.
    */
  def createGameStateTransformer(gameState: GameState): GameStateTransformer

  def createAndApplyGameStateTransformer(gameState: GameState): GameState =
    createGameStateTransformer(gameState)(gameState)

  /** Returns whether this action is legal at that particular point in time, i.e., for that
    * [[gamelogic.gamestate.GameState]]. If the action is not legal, it returns an error message saying why.
    */
  def isLegal(gameState: GameState): Option[String] =
    Option.unless(canHappen(gameState))(s"This action $this is not valid.")

  def canHappen(gameState: GameState): Boolean = true

  /** We compare ids if the time are the same so that there never is ambiguity. */
  final def compare(that: GameAction): Int = this.time compare that.time match {
    case 0 => this.actionId compare that.actionId
    case x => x
  }

  def setId(newId: GameAction.Id): GameAction

  def changeTime(newTime: Long): GameAction

}

object GameAction {

  trait EntityCreatorAction {
    def entityId: Entity.Id
  }

  opaque type Id = Long

  object Id:
    extension (id: Id) @inline def toLong: Long = id: Long

    def apply(long: Long): Id = long

    def initial: Id = 0L

    given Ordering[Id] = Ordering[Long]

  implicit val gameActionPickler: Pickler[GameAction] = null

  def newId()(using gen: IdGeneratorContainer): GameAction.Id = gen.gameActionIdGenerator.nextId()

}
