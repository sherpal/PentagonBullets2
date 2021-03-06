package utils.laminarzio

import com.raquo.laminar.api.A._
import zio.stream._
import zio.{CancelableFuture, URIO, ZIO}

object Implicits {

  type InnerForGlobalEnv[A] = URIO[frontend.GlobalEnv, A]

  implicit val zioFlattenStrategy: FlattenStrategy[Observable, InnerForGlobalEnv, EventStream] =
    new FlattenStrategy[Observable, InnerForGlobalEnv, EventStream] {
      def flatten[A](parent: Observable[InnerForGlobalEnv[A]]): EventStream[A] =
        parent.flatMap(task =>
          EventStream.fromFuture(
            frontend.runtime.unsafeRunToFuture(task)
          )
        )
    }

  implicit class EventStreamObjEnhanced(es: EventStream.type) {

    /** Retrieve the result of the zio effect and send it through the laminar stream
      */
    def fromZIOEffect[A](effect: ZIO[frontend.GlobalEnv, Throwable, A]): EventStream[A] =
      EventStream.fromFuture(frontend.runtime.unsafeRunToFuture(effect))

    /** Passes the outputs of the incoming [[zio.stream.ZStream]] into a laminar stream. /!\ The ZStream will continue
      * to run, even after the laminar stream is over with it. The returned [[zio.CancelableFuture]] allows you to
      * cancel it.
      *
      * I think this is not fantastic. We should do it in another way, probably.
      */
    def fromZStream[A](ztream: Stream[Nothing, A]): (CancelableFuture[Unit], EventStream[A]) = {
      val bus = new EventBus[A]
      zio.Runtime.default.unsafeRunToFuture(ztream.foreach(elem => ZIO.effectTotal(bus.writer.onNext(elem)))) ->
        bus.events
    }

  }

  implicit class EventStreamEnhanced[A](es: EventStream[A]) {}

}
