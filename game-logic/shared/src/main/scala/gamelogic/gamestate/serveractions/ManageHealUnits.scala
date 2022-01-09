package gamelogic.gamestate.serveractions

import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.gamestate.gameactions.*
import gamelogic.utils.IdGeneratorContainer
import gamelogic.entities.ActionSource.ServerSource

object ManageHealUnits extends ServerActionFromActionList {

  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    println(getClass)

    val time      = nowGenerator()
    val gameState = currentState.currentGameState
    val healUnits = gameState.healUnits

    val removeOldHealUnits = healUnits.values.toList
      .filter(time - _.time > HealUnit.lifeTime)
      .map(unit => DestroyHealUnit(actionId(), time, unit.id, ServerSource))

    val healingUnits = healUnits.values
      .map(unit =>
        gameState.players.values.find(player => unit.collides(player, time)).map { player =>
          PlayerTakeHealUnit(actionId(), time, player.id, unit.id, ServerSource)
        }
      )
      .collect { case Some(action) => action }

    removeOldHealUnits ++ healingUnits
  }
}
