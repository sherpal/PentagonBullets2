package models.playing

import gamelogic.abilities.Ability
import gamelogic.entities.concreteentities.Player
import be.doeraene.physics.Complex
import models.playing.Controls.InputCode

/** A [[UserInput]] corresponds to the input a user made, translated to something meaningful from the point of view of
  * the game.
  *
  * For example, the input of a direction, or using an ability.
  */
sealed trait UserInput {
  def isKnown: Boolean = true
}

object UserInput {

  sealed trait DirectionInput extends UserInput {
    def direction: Complex
  }
  case object Up extends DirectionInput {
    def direction: Complex = Complex.i
  }
  case object Down extends DirectionInput {
    def direction: Complex = -Complex.i
  }
  case object Right extends DirectionInput {
    def direction: Complex = 1
  }
  case object Left extends DirectionInput {
    def direction: Complex = -1
  }

  /** Pressed when user wants to shoot bullets. */
  case object DefaultBullets extends UserInput

  /** Pressed together with [[DefaultBullets]] when the user wants to shoot bullets at a higher rate. */
  case object BurstBullets extends UserInput

  /** Pressed when the user wants to use their [[gamelogic.abilities.ActivateShield]] ability. */
  case object ShieldAbility extends UserInput

  /** Represent */
  case class AbilityInput(abilityIndex: Int) extends UserInput {
    def abilityId(player: Player): Option[Ability.AbilityId] =
      player.abilities.toList.drop(abilityIndex).headOption
  }

  final val directions: List[DirectionInput] = List(Up, Down, Right, Left)

  /** If the key code from the event is not above, you can keep track with this unknown instance. */
  case class Unknown(code: InputCode) extends UserInput {
    override def isKnown: Boolean = false
  }

  def movingDirection(pressedInputs: Set[UserInput]): Complex =
    directions.filter(pressedInputs.contains).map(_.direction).sum.safeNormalized

}
