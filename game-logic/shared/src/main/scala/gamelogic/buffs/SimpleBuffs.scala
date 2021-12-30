package gamelogic.buffs

import gamelogic.entities.Entity

/** A lot of buffs (especially the "never ending" ones) simply require
  *   - a bearerId [[gamelogic.entities.Entity.Id]]
  *   - an appearance time (Long)
  *
  * All these buffs will be grouped into a single [[gamelogic.gamestate.GameAction]] so that we drastically reduce the
  * number of game actions needed to be created.
  */
object SimpleBuffs {

  final val simpleBuffs: Map[Buff.ResourceIdentifier, (Buff.Id, Entity.Id, Entity.Id, Long) => Buff] = Map(
  )

  def apply(
      identifier: Buff.ResourceIdentifier,
      buffId: Buff.Id,
      bearerId: Entity.Id,
      sourceId: Entity.Id,
      appearanceTime: Long
  ): Option[Buff] =
    simpleBuffs.get(identifier).map(_(buffId, bearerId, sourceId, appearanceTime))

}
