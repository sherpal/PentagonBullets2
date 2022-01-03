package utils.websocket

import com.raquo.airstream.ownership.Owner
import com.raquo.laminar.api.A.*
import org.scalajs.dom.Event
import zio.CancelableFuture

import scala.language.implicitConversions

trait Socket[In, Out] {
  val $in: EventStream[In]
  val outWriter: WriteBus[Out]
  val $closed: EventStream[Unit]
  val $error: EventStream[Event]
  val $open: EventStream[Event]

  def open()(using Owner): CancelableFuture[Unit]

  def close(): Unit

}

object Socket {

  implicit def asObserver[Out](socket: Socket[_, Out]): Observer[Out] = socket.outWriter

}
