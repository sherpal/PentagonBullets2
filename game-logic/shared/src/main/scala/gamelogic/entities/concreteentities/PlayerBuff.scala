package gamelogic.entities.concreteentities

/** A PlayerBuff is an ActionChanger attached to a particular
  */
trait PlayerBuff extends ActionChanger {

  val playerId: Long

}
