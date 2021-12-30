package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.concreteentities.BulletAmplifier
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}

/** Adds a BulletAmplifier to the game.
  */
final case class NewBulletAmplifier(
    actionId: GameAction.Id,
    time: Long,
    id: Entity.Id,
    ownerId: Entity.Id,
    rotation: Double,
    pos: Complex,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(
      BulletAmplifier(
        id,
        time,
        ownerId,
        pos.re,
        pos.im,
        BulletAmplifier.bulletAmplifierShape,
        rotation,
        Nil
      ),
      time
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
