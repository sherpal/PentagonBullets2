package gamelogic.gamestate.gameactions

import gamelogic.entities.ActionSource
import gamelogic.buffs.SimpleTickerBuff
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.GameAction.Id
import gamelogic.gamestate.statetransformers.{GameStateTransformer, WithBuff}

final case class PutSimpleTickerBuff(
    actionId: GameAction.Id,
    time: Long,
    buff: SimpleTickerBuff,
    actionSource: ActionSource
) extends GameAction {

  def setId(newId: GameAction.Id): GameAction = copy(actionId = newId)

  def changeTime(newTime: Long): GameAction = copy(time = newTime)

  def createGameStateTransformer(gameState: GameState): GameStateTransformer =
    new WithBuff(buff.changeLastTickTime(time))

}
