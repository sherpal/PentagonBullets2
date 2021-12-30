package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.SmashBullet
import gamelogic.gamestate.statetransformers.*
import gamelogic.gamestate.{GameAction, GameState}

final case class NewSmashBullet(
    actionId: GameAction.Id,
    time: Long,
    bulletId: Entity.Id,
    ownerId: Entity.Id,
    pos: Complex,
    dir: Double,
    radius: Int,
    speed: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    new WithEntity(
      SmashBullet(
        bulletId,
        time,
        ownerId,
        pos.re,
        pos.im,
        radius,
        dir,
        speed,
        0,
        time
      ),
      time
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}
