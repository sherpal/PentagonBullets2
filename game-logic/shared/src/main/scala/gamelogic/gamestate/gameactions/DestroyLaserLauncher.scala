package gamelogic.gamestate.gameactions

import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.{GameAction, GameState}

final case class DestroyLaserLauncher(
    actionId: GameAction.Id,
    time: Long,
    laserLauncherId: Entity.Id,
    actionSource: ActionSource
) extends GameAction
    with DestroyEntity {

  def entityId: Entity.Id = laserLauncherId

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

}
