package game

import com.raquo.laminar.api.L._
import be.doeraene.physics.Complex
import models.playing.UserInput
import typings.std.MouseEvent

/** This class combines all different input sources that the user can use. Currently only Keyboard and Mouse are
  * supported, but maybe in the future game controllers could be used as well.
  */
final class UserControls(keyboard: Keyboard, mouse: Mouse) {

  val downInputs: EventStream[UserInput] = EventStream.merge(keyboard.downUserInputEvents, mouse.downUserInputEvents)
  val upInputs: EventStream[UserInput]   = EventStream.merge(keyboard.upUserInputEvents, mouse.upUserInputEvents)

  val $pressedUserInput: Signal[Set[UserInput]] =
    EventStream
      .merge(
        downInputs.map(_ -> true),
        upInputs.map(_   -> false)
      )
      .foldLeft(Set.empty[UserInput]) { case (accumulated, (input, isDown)) =>
        if isDown then accumulated + input else accumulated - input
      }

  def $effectiveMousePosition: EventStream[Complex] = mouse.$effectiveMousePosition

  def $mouseClicks: EventStream[MouseEvent] = mouse.$mouseClicks

  def leftClickDownEvents: EventStream[Unit] = mouse.leftClickDownEvents
  def leftClickUpEvents: EventStream[Unit]   = mouse.leftClickUpEvents

  def effectiveMousePos(event: MouseEvent): Complex = mouse.effectiveMousePos(event)

}
