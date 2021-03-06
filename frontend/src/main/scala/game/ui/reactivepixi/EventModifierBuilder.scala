package game.ui.reactivepixi

import com.raquo.laminar.api.A._
import game.ui.reactivepixi.EventModifierBuilder.ReactiveInteractionEvent
import game.ui.reactivepixi.ReactivePixiElement.Base
import typings.pixiJs.PIXI.interaction.{InteractionEvent, InteractionEventTypes}

import scala.scalajs.js

/** This is thought to be a builder for [[PixiModifier]] involving JavaScript events, such as an `onClick`. However,
  * this particular design is not reflected in the type system, with the exception of the `stopPropagation` method.
  *
  * Attaching a "event handler" to a reactive element is done through this class, and the events, augmented with the
  * [[ReactivePixiElement]], are sent to an [[com.raquo.airstream.core.Observer]]. However, the event content can be
  * modified before entering the observer, via methods such as
  *   - map or mapTo
  *   - stopPropagation
  *   - withCurrentValueOf
  *
  * The design is somewhat different than the one used in Laminar, where accessing the element is done via a context,
  * but I think it is consistent enough for Pixi.
  *
  * Note: having these "event handlers" to work often requires that `interactive` is set to true on the display object.
  * Turning interactive on and off is better than disabling or enabling the events yourself.
  *
  * @tparam El
  *   type of the [[ReactivePixiElement]] affected by the built modifier
  * @tparam T
  *   type of elements entering in the observer.
  */
trait EventModifierBuilder[-El <: ReactivePixiElement.Base, T] {

  def map[U](f: T => U): EventModifierBuilder[El, U] =
    EventModifierBuilder.factory { (observer: Observer[U]) =>
      this --> observer.contramap(f)
    }

  def mapTo[U](u: => U): EventModifierBuilder[El, U] = map(_ => u)

  def -->(observer: Observer[T]): PixiModifier[El]

  def stopPropagation(implicit ev: T <:< ReactiveInteractionEvent[_]): EventModifierBuilder[El, T] =
    map { event =>
      ev(event).event.stopPropagation()
      event
    }

  def withCurrentValueOf[U](signal: Signal[U]): EventModifierBuilder[El, (T, U)] = {
    val outerThis = this
    EventModifierBuilder.factory { (observer: Observer[(T, U)]) =>
      new PixiModifier[El] {
        def apply(element: El): Unit = {
          val strictSignal = signal.observe(element)
          (outerThis --> observer.contramap[T](t => (t, strictSignal.now()))).apply(element)
        }
      }
    }
  }

}

object EventModifierBuilder {

  private def factory[El <: ReactivePixiElement.Base, T](
      observerToModifier: Observer[T] => PixiModifier[El]
  ): EventModifierBuilder[El, T] =
    (observer: Observer[T]) => observerToModifier(observer)

  case class ReactiveInteractionEvent[El <: ReactivePixiElement.Base](
      element: El,
      event: InteractionEvent
  )

  val onClick: EventModifierBuilder[Base, ReactiveInteractionEvent[Base]] = onClickFor[Base]

  def onClickFor[El <: Base]: EventModifierBuilder[El, ReactiveInteractionEvent[El]] =
    new EventModifierBuilder[El, ReactiveInteractionEvent[El]] {
      def -->(observer: Observer[ReactiveInteractionEvent[El]]): PixiModifier[El] = new PixiModifier[El] {
        def apply(element: El): Unit = {

          val listener: js.Function1[InteractionEvent, Unit] = { event =>
            observer.onNext(ReactiveInteractionEvent(element, event))
          }

          element.ref.addListener(InteractionEventTypes.click, listener)
        }
      }
    }

}
