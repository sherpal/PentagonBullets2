package game

import com.raquo.laminar.api.A._
import models.playing.{Controls, KeyboardControls, UserInput}
import org.scalajs.dom
import typings.std.KeyboardEvent
import utils.domutils.Implicits._

import scala.scalajs.js

/** Facility methods for having access to keyboard event.
  *
  * Note: each instance of this class will add an event listener to the document.
  */
final class Keyboard(controls: Controls) {

  private val downKeyEventBus: EventBus[KeyboardEvent] = new EventBus[KeyboardEvent]
  private val upKeyEventBus: EventBus[KeyboardEvent]   = new EventBus[KeyboardEvent]

  /** Stream of key-press events */
  val $downKeyEvents: EventStream[KeyboardEvent] = downKeyEventBus.events

  /** Stream of key-up events */
  val $upKeyEvents: EventStream[KeyboardEvent] = upKeyEventBus.events

  /** Merged key-press and key-up events. */
  val $keyboardEvents: EventStream[KeyboardEvent] = EventStream
    .merge($downKeyEvents, $upKeyEvents)
    .filterNot(_.repeat)

  /** Signal of all currently pressed key codes. */
  val $pressedKeys: Signal[Set[String]] = $keyboardEvents.foldLeft(Set.empty[String]) {
    case (accumulatedSet, event) if event.`type` == "keyup"   => accumulatedSet - event.code
    case (accumulatedSet, event) if event.`type` == "keydown" => accumulatedSet + event.code
    case (s, _)                                               => s // should never happen as we don't register it
  }

  val downUserInputEvents: EventStream[UserInput] = $keyboardEvents.filter(_.`type` == "keydown")
    .map(_.toInputCode)
    .map(controls.getOrUnknown)
  val upUserInputEvents: EventStream[UserInput] = $keyboardEvents.filter(_.`type` == "keyup")
    .map(_.toInputCode)
    .map(controls.getOrUnknown)

  private val keyDownHandler: js.Function1[dom.KeyboardEvent, _] = (event: dom.KeyboardEvent) => {
    event.stopPropagation()
    event.preventDefault()
    downKeyEventBus.writer.onNext(event.asInstanceOf[KeyboardEvent])
  }
  private val keyUpHandler: js.Function1[dom.KeyboardEvent, _] = (event: dom.KeyboardEvent) => {
    event.stopPropagation()
    event.preventDefault()
    upKeyEventBus.writer.onNext(event.asInstanceOf[KeyboardEvent])
  }

  dom.document.addEventListener("keydown", keyDownHandler)
  dom.document.addEventListener("keyup", keyUpHandler)

  /** Removes the handlers on the document. This class will not work anymore after calling this. */
  def destroy(): Unit = {
    dom.document.removeEventListener("keydown", keyDownHandler)
    dom.document.removeEventListener("keyup", keyUpHandler)
  }

}
