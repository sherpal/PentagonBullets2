package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

/** GameAction when a player shoots a Bullet
  *
  * @param playerId
  *   ID of the player that shoots the player
  * @param pos
  *   starting position of the Bullet
  * @param dir
  *   direction in which the Bullet goes
  * @param speed
  *   the speed of the Bullet. Usually Bullet.speed.
  * @param time
  *   time at which the Bullet was fired
  * @param travelledDistance
  *   the distance already travelled by the bullet.
  */
final case class NewBullet(
    actionId: GameAction.Id,
    id: Entity.Id,
    playerId: Entity.Id,
    teamId: Int,
    pos: Complex,
    radius: Int,
    dir: Double,
    speed: Int,
    time: Long,
    travelledDistance: Double,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(
      Bullet(
        id,
        time,
        playerId,
        teamId,
        pos.re,
        pos.im,
        radius,
        dir,
        speed,
        travelledDistance
      ),
      time
    )

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

}

object NewBullet {

  @inline def bulletPrice: Double = 8.0

}
