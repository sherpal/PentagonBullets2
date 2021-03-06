package utils.domutils

import typings.std.KeyboardEvent
import models.playing.Controls._

object Implicits {

  implicit class KeyboardEventEnhanced(keyboardEvent: KeyboardEvent) {
    def toInputCode: InputCode = KeyCode(keyboardEvent.code)
  }

}
