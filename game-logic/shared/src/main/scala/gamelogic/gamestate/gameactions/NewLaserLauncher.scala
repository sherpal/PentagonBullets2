package gamelogic.gamestate.gameactions

import be.doeraene.physics.Complex
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.entities.concreteentities.LaserLauncher
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithEntity}
import gamelogic.gamestate.{GameAction, GameState}

final case class NewLaserLauncher(
    actionId: GameAction.Id,
    time: Long,
    laserLauncherId: Entity.Id,
    pos: Complex,
    ownerId: Entity.Id,
    actionSource: ActionSource
) extends GameAction {

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    WithEntity(
      LaserLauncher(
        laserLauncherId,
        pos,
        ownerId
      ),
      time
    )

  def setId(newId: GameAction.Id): NewLaserLauncher = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

}
