package gamelogic.buffs

/** [[SimplePassiveBuff]] is a kind of a marker trait used for buffs that can be used in
  * [[gamelogic.gamestate.gameactions.PutSimplePassiveBuff]] action.
  *
  * This marker trait will be used for serialization (probably boopickle)
  */
trait SimplePassiveBuff extends PassiveBuff
