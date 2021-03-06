package game.ui.reactivepixi

import com.raquo.laminar.api.A._
import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import be.doeraene.physics.Complex
import org.scalajs.dom
import typings.pixiJs.PIXI.interaction.{InteractionEvent, InteractionEventTypes}
import typings.pixiJs.PIXI.{Container, IHitArea}
import typings.pixiJs.mod.{Application, Rectangle}
import typings.std.MouseEvent
import utils.domutils.ScalablyTypedScalaJSDomInterop.given

import scala.scalajs.js

/** Reactive container based on the application stage.
  *
  * This class is intended to be a singleton. Creating several of this class with the same application might have
  * surprising effects.
  */
final class ReactiveStage(val application: Application) extends ReactivePixiElement.ReactiveContainer {

  /** Camera to be used inside the game, given for free. You're obviously not forced to use it. If you don't, the
    * `clickEventsWorldPositions` event stream will probably not do what you want.
    */
  val camera: Camera = new Camera(application.view)

  override val ref: Container = application.stage

  if (scala.scalajs.LinkingInfo.developmentMode) {
    dom.window.asInstanceOf[js.Dynamic].updateDynamic("pixi_stage_scala")(ref)
  }

  def setHitArea(): Unit =
    ref.hitArea = new Rectangle(0, 0, application.view.width, application.view.height).asInstanceOf[IHitArea]

  ref.interactive = true
  setHitArea()

  /** Facility method when using the stage.
    */
  def apply(modifiers: PixiModifier[ReactiveContainer]*): ReactiveStage = amend(modifiers: _*)

  private val clickEventBus: EventBus[InteractionEvent] = new EventBus[InteractionEvent]
  ref.addListener(
    InteractionEventTypes.click,
    { (event: InteractionEvent) =>
      println("Stage has been clicked on")
      clickEventBus.writer.onNext(event)
    }
  )

  /** Emits all click events that happen on the stage.
    *
    * You can stop propagation on events if you don't want the events to be emitted via this stream. This can be useful,
    * for example, if you don't want this to fire when the user clicks on the GUI.
    */
  val clickEvents: EventStream[InteractionEvent] = clickEventBus.events

  /** Returns the `Complex(x, y)` coordinates in the canvas where a click event occur.
    */
  val clickEventsPositions: EventStream[Complex] = clickEvents
    .map(event => event.data.originalEvent.asInstanceOf[MouseEvent])
    .map(mouseEvent => (mouseEvent.clientX, mouseEvent.clientY))
    .map { case (x, y) =>
      val bounds = application.view.getBoundingClientRect()
      Complex(x - bounds.left, y - bounds.top)
    }

  /** Returns the world position at which click event occurred.
    */
  val clickEventsWorldPositions: EventStream[Complex] =
    clickEventsPositions.map(camera.canvasToComplex).map(camera.mousePosToWorld)

  private val resizeEventBus = new EventBus[(Double, Double)]

  /** Emits new width and height of the underlying canvas when it is resized.
    */
  val resizeEvents: Signal[(Double, Double)] = resizeEventBus.events.toSignal(
    (
      application.view.width,
      application.view.height
    )
  )

  /** This function *has* to be called on every size change of the application view. Not calling this will result in
    * most of the sprites being off in position.
    */
  def resize(): Unit = {
    setHitArea()
    resizeEventBus.writer.onNext((application.view.width, application.view.height))
  }

  /** Computes the width and height to give to the game canvas so that it takes a maximum place in the given windowWidth
    * x windowHeight rectangle, while keeping the ratio width / height to desiredRatio.
    * @param windowWidth
    *   current width of the window
    * @param windowHeight
    *   current height of the window
    * @param desiredRatio
    *   width over height ratio for the game
    * @return
    *   width and height to give to the canvas in order to satisfy the above conditions.
    */
  def computeApplicationViewDimension(
      windowWidth: Double,
      windowHeight: Double,
      desiredRatio: Double
  ): (Double, Double) = {
    val width  = windowWidth min (windowHeight * desiredRatio)
    val height = width / desiredRatio

    (width, height)
  }

}
