package gamelogic.gamestate.serveractions

import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.gamestate.gameactions.*
import gamelogic.entities.concreteentities.*
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.utils.IdGeneratorContainer

object ManageDamageZones extends ServerActionFromActionList {

  def createActionList(
      currentState: ActionGatherer,
      nowGenerator: () => Long
  )(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): Iterable[GameAction] = {
    val time      = nowGenerator()
    val gameState = currentState.currentGameState

    val damageZones = gameState.damageZones.values

    val growingDamageZones = damageZones
      .filter(time - _.lastGrow > DamageZone.growingRate)
      .map(zone =>
        UpdateDamageZone(
          actionId(),
          time,
          zone.id,
          time,
          zone.lastTick,
          zone.pos,
          zone.radius + DamageZone.growingValue,
          ServerSource
        )
      )

    val playersHitByDamageZones = damageZones
      .filter(time - _.lastTick > DamageZone.tickRate)
      .flatMap { zone =>
        UpdateDamageZone(
          actionId(),
          time,
          zone.id,
          zone.lastGrow,
          time,
          zone.pos,
          zone.radius,
          ServerSource
        ) +:
          gameState.players.values.toList
            .filter(player => player.collides(zone, time))
            .map(player =>
              PlayerTakeDamage(
                actionId(),
                time,
                player.id,
                zone.id,
                DamageZone.damageOnTick,
                ServerSource
              )
            )
      }

    val removeTooBigDamageZones = damageZones
      .filter(_.radius > DamageZone.maxRadius)
      .map(zone => DestroyDamageZone(actionId(), time, zone.id, ServerSource))

    growingDamageZones ++ playersHitByDamageZones ++ removeTooBigDamageZones
  }
}
