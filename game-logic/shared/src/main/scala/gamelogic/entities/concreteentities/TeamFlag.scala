package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import gamelogic.gamestate.GameState
import be.doeraene.physics.shape.Circle
import gamelogic.entities.{concreteentities, Body, Entity}

final case class TeamFlag(
    id: Entity.Id,
    xPos: Double,
    yPos: Double,
    teamNbr: Int,
    bearerId: Option[Entity.Id],
    takenBy: List[Int]
) extends Body {

  def time: Long = 0L // a bit weird

  def pos: Complex = Complex(xPos, yPos)

  def withBearer(bearerId: Entity.Id): TeamFlag = copy(bearerId = Some(bearerId))

  def withoutBearer: TeamFlag = copy(bearerId = None)

  def withNewTakenBy(teamId: Int): TeamFlag = copy(takenBy = takenBy :+ teamId)

  val shape: Circle = TeamFlag.flagShape

  val rotation: Double = 0

  def currentPosition(gameState: GameState, time: Long): Complex = bearerId match {
    case Some(playerId) =>
      gameState.entityByIdAs[Player](playerId).get.currentPosition(time, gameState.obstacles.values)
    case None =>
      pos
  }

  def isBorn: Boolean = bearerId.isDefined

}

object TeamFlag {

  val radius: Int = 20

  val flagShape: Circle = Circle(radius)

  def scores(gameState: GameState): Map[Int, Int] = {
    val flags             = gameState.allTEntities[TeamFlag].values.toList
    val teamsThatTookFlag = flags.flatMap(flag => flag.takenBy)

    flags.map(_.teamNbr).map(n => n -> teamsThatTookFlag.count(_ == n)).toMap
  }

}
