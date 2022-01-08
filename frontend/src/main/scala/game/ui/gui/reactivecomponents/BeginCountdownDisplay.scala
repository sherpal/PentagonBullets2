package game.ui.gui.reactivecomponents

import com.raquo.laminar.api.A.*
import game.ui.reactivepixi.AttributeModifierBuilder.*
import game.ui.reactivepixi.ReactivePixiElement.pixiText
import gamelogic.gamestate.GameState
import be.doeraene.physics.Complex
import gamecommunication.ServerToClient.BeginIn
import typings.pixiJs.anon.Align
import typings.pixiJs.mod.TextStyle
import utils.misc.RGBColour

import scala.concurrent.duration.FiniteDuration

/** Displays the time since the beginning of the game (in minutes:seconds) at the given position.
  *
  * @param updates
  *   stream of game state with current time. The clock is re-computed each time the stream emits. That means that it's
  *   useless to give a stream emitting more than once per second.
  * @param positions
  *   signal with the (top left) position of the container in the canvas.
  */
final class BeginCountdownDisplay(
    beginInEvents: EventStream[BeginIn],
    updates: EventStream[Long],
    positions: Signal[Complex]
) extends GUIComponent {

  private val remainingTimeInMillis: EventStream[Long] =
    updates
      .startWith(0L)
      .combineWithFn(
        beginInEvents.map(_.millis).startWith(0L),
        beginInEvents.map(_ => System.currentTimeMillis()).startWith(System.currentTimeMillis())
      )((now, startsInMillis, startTime) => startsInMillis - (now - startTime))
      .changes

  private val remainingSecondsToOneDigits = remainingTimeInMillis.map(_ / 100L).map(_ / 10.0)

  container.amend(
    visible  <-- remainingTimeInMillis.map(_ > 0),
    position <-- positions,
    pixiText(
      "",
      text <-- remainingSecondsToOneDigits.map(seconds =>
        if seconds > 1 then seconds.toInt.toString else seconds.toString
      ),
      textStyle := new TextStyle(
        Align()
          .setFontSize(30)
          .setFill(RGBColour.white.rgb)
      )
    )
  )

}
