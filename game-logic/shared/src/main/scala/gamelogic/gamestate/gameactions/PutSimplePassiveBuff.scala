package gamelogic.gamestate.gameactions

import gamelogic.entities.ActionSource
import gamelogic.buffs.SimplePassiveBuff
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.GameAction.Id
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithBuff}

final case class PutSimplePassiveBuff(
    actionId: GameAction.Id,
    time: Long,
    buff: SimplePassiveBuff,
    actionSource: ActionSource
) extends GameAction {

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    GameStateTransformer.when(_.isPlayerAlive(buff.bearerId))(new WithBuff(buff))

}
