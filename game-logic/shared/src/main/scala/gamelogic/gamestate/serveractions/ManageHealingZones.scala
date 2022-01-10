package gamelogic.gamestate.serveractions
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.utils.IdGeneratorContainer
import gamelogic.gamestate.gameactions.*
import gamelogic.entities.concreteentities.*
import gamelogic.entities.ActionSource.ServerSource

object ManageHealingZones extends ServerActionFromActionList {
  override def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    val time            = nowGenerator()
    val gameState       = currentState.currentGameState
    val healingZones    = gameState.healingZones.values
    val teamsByPlayerId = gameState.teamsByPlayerId

    val tickingZones = healingZones
      .filter(time - _.lastTick > HealingZone.tickRate)
      .flatMap { zone =>
        val team = teamsByPlayerId(zone.ownerId)
        UpdateHealingZone(
          actionId(),
          time,
          zone.id,
          zone.ownerId,
          zone.lifeSupply,
          zone.pos,
          ServerSource
        ) +:
          team.playerIds
            .filter(gameState.isPlayerAlive)
            .map(gameState.players(_))
            .filter(player => player.collides(zone, time))
            .map(player =>
              HealingZoneHeals(actionId(), time, player.id, zone.id, HealingZone.healingOnTick, ServerSource)
            )
            .take(zone.ticksRemaining)
      }

    val removedHealingZones = gameState.healingZones.values
      .filter(zone => zone.lifeSupply <= 0 || time - zone.creationTime > HealingZone.lifetime)
      .map(zone => DestroyHealingZone(actionId(), time, zone.id, ServerSource))

    tickingZones ++ removedHealingZones
  }
}
