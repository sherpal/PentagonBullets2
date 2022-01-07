package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{Circle, Shape}
import gamelogic.entities.{Body, Entity}
import gamelogic.gamestate.GameState

import scala.annotation.tailrec
import scala.util.Random

/** A HealUnit pops at random places during the Game and heal players for some small amount.
  */
final case class HealUnit(id: Entity.Id, time: Long, pos: Complex) extends Body {

  val shape: Shape = Circle(HealUnit.radius)

  val rotation: Double = 0.0

}

object HealUnit {
  val radius: Double = 10

  val lifeTime: Long = 15000 // 15 seconds before disappearing

  val lifeGain: Double = 15 // restore 15 life points when a player takes it.

  def playerTakeUnit(player: Player, time: Long): Player =
    player.copy(lifeTotal = math.min(player.lifeTotal + lifeGain, Player.maxLife))

  @tailrec
  def randomPosition(gameState: GameState): Complex = {
    val pos = gameState
      .findPositionInMist()
      .getOrElse(
        gameState.gameBounds.randomPointIn((realRange, imRange) =>
          Complex(
            Random.between(realRange._1, realRange._2),
            Random.between(imRange._1, imRange._2)
          )
        )
      )
    val dummyHealUnit = HealUnit(Entity.Id.initial, 0, pos)

    if gameState.obstacles.values.exists(_.collides(dummyHealUnit, 0)) then randomPosition(gameState)
    else pos
  }
}
