package models.playing

import models.playing.Controls.InputCode
import models.syntax.Pointed
import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto._

/** Gathers all possible inputs that the user assigned. The [[InputCode]]s in argument have the knowledge of what device
  * is the source of the input.
  */
final case class Controls(
    upKey: InputCode,
    downKey: InputCode,
    leftKey: InputCode,
    rightKey: InputCode,
    defaultBulletAbilityKey: InputCode,
    burstBulletsAbilityKey: InputCode,
    chosenAbilityKey: InputCode,
    shieldAbilityKey: InputCode,
    abilityKeys: List[InputCode]
) {

  lazy val controlMap: Map[InputCode, UserInput] = Map(
    upKey                   -> UserInput.Up,
    downKey                 -> UserInput.Down,
    leftKey                 -> UserInput.Left,
    rightKey                -> UserInput.Right,
    defaultBulletAbilityKey -> UserInput.DefaultBullets,
    burstBulletsAbilityKey  -> UserInput.BurstBullets,
    shieldAbilityKey        -> UserInput.ShieldAbility,
    chosenAbilityKey        -> UserInput.AbilityInput(0)
  ) ++ abilityKeys.zipWithIndex.map { case (code, idx) => code -> UserInput.AbilityInput(idx) }.toMap

  def allKeysInMultiple: List[InputCode] =
    (List(
      upKey,
      downKey,
      rightKey,
      leftKey,
      burstBulletsAbilityKey,
      shieldAbilityKey,
      chosenAbilityKey,
      defaultBulletAbilityKey
    ) ++ abilityKeys)
      .groupBy(identity)
      .filter(_._2.length > 1)
      .keys
      .toList

  /** Maybe returns a control key which is assigned twice. */
  def maybeMultipleKey: Option[InputCode] = allKeysInMultiple.headOption

  /** Retrieve the [[UserInput]] for this keyCode, or the [[UserInput.Unknown]] if it is not defined. */
  def getOrUnknown(keyCode: InputCode): UserInput = controlMap.getOrElse(keyCode, UserInput.Unknown(keyCode))
}

object Controls {

  sealed trait InputSource {
    type Code
  }
  case object MouseSource extends InputSource {
    type Code = Int
  }
  case object KeyboardSource extends InputSource {
    type Code = String
  }

  /** Represent an operation when the user gives input via one of the sources. */
  sealed trait InputCode {
    val source: InputSource
    def code: source.Code
    def label: String
  }

  object InputCode {
    private case class Tpe(tpe: String)

    implicit def codec: Codec[InputCode] = deriveCodec
  }

  sealed trait KeyInputModifier {
    def name: String
  }
  object KeyInputModifier {
    case object WithShift extends KeyInputModifier {
      def name: String = "Shift"
    }
  }
  case class KeyCode(code: String) extends InputCode {
    val source: KeyboardSource.type = KeyboardSource
    def label: String               = code
  }
  case class ModifiedKeyCode(code: String, modifier: KeyInputModifier) extends InputCode {
    val source: KeyboardSource.type = KeyboardSource
    def label                       = s"${modifier.name} ${code}"
  }
  case class MouseCode(code: Int) extends InputCode {
    val source: MouseSource.type = MouseSource
    def label                    = s"Button $code"
  }

  implicit val pointed: Pointed[Controls] = Pointed.factory(
    Controls(
      KeyCode("KeyW"),
      KeyCode("KeyS"),
      KeyCode("KeyA"),
      KeyCode("KeyD"),
      MouseCode(0),
      KeyCode("ShiftLeft"),
      MouseCode(2),
      KeyCode("KeyE"),
      (1 to 10).map(_ % 10).map("Digit" + _).map(KeyCode.apply).toList
    )
  )

  val storageKey = "pentagonBulletsControls"

}
