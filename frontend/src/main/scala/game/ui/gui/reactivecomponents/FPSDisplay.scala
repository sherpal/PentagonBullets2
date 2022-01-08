package game.ui.gui.reactivecomponents

import com.raquo.laminar.api.A.*
import game.ui.reactivepixi.AttributeModifierBuilder.*
import game.ui.reactivepixi.ReactivePixiElement.*
import typings.pixiJs.anon.Align
import typings.pixiJs.mod.TextStyle
import utils.misc.RGBColour

import scala.collection.immutable.Queue

/** Small pixi component for displaying the frame rate, computed over 100 ticks.
  *
  * @param ticks
  *   [[com.raquo.airstream.eventstream.EventStream]] with message each time the frames are updated. typed to
  *   [[scala.Any]] since we don't care what we get. We could type to [[scala.Unit]] but that often requires mapping
  *   once more for no reason.
  */
final class FPSDisplay(ticks: EventStream[Any]) extends GUIComponent {

  val fps: EventStream[Long] = ticks
    .mapTo(System.currentTimeMillis())
    .foldLeft(Queue[Long](System.currentTimeMillis())) { (queue, newTime) =>
      val newQueue = queue.enqueue(newTime)
      if newQueue.size > 100 then newQueue.dequeue._2 else newQueue
    }
    .changes
    .map(queue => 1000 * queue.size.toDouble / (queue.last - queue.head))
    .map(math.round)
    .map(_ / 4 * 4) // rounding to change less often.

  container.amend(
    pixiText(
      "",
      text <-- fps.map(_.toString).toSignal(""),
      textStyle := new TextStyle(Align().setFontSize(30.0).setFill(RGBColour.white.rgb)),
      x := 10,
      y := 10
    )
  )

}
