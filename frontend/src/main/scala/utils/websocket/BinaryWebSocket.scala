package utils.websocket

import com.raquo.airstream.eventbus.{EventBus, WriteBus}
import com.raquo.laminar.api.L.*
import com.raquo.airstream.ownership.Owner
import org.scalajs.dom
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.{Event, WebSocket}
import urldsl.language.QueryParameters.dummyErrorImpl.*
import urldsl.language.*
import zio.{CancelableFuture, UIO, ZIO}
import boopickle.Default.*

import java.nio.ByteBuffer
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.typedarray.ArrayBuffer
import scala.scalajs.js.typedarray.TypedArrayBufferOps.*
import scala.scalajs.js.typedarray.TypedArrayBuffer

/** Prepares a WebSocket to connect to the specified url. The connection actually occurs when you run the `open` method.
  *
  * Messages coming from the server can be retrieved using the `$in` [[com.raquo.airstream.eventstream.EventStream]] and
  * sending messages to the server can be done by writing to the `outWriter` [[com.raquo.airstream.core.Observer]]
  */
final class BinaryWebSocket[In, Out] private (
    path: String,
    host: String
)(implicit
    decoder: Pickler[In],
    encoder: Pickler[Out]
) {
  private def url: String = "ws://" + host + "/ws/" + path

  private lazy val socket = new WebSocket(url)

  private val inBus: EventBus[In]           = new EventBus
  private val outBus: EventBus[Out]         = new EventBus
  private val closeBus: EventBus[Unit]      = new EventBus
  private val errorBus: EventBus[dom.Event] = new EventBus
  private val openBus: EventBus[dom.Event]  = new EventBus

  private def openWebSocketConnection(implicit owner: Owner) =
    for {
      webSocket <- UIO(socket)
      _ <- UIO {
        webSocket.onmessage = (event: MessageEvent) =>
          inBus.writer.onNext(
            Unpickle[In].fromBytes(TypedArrayBuffer.wrap(event.data.asInstanceOf[ArrayBuffer]))
          )
      }
      _ <- UIO {
        outBus.events.map(Pickle.intoBytes).map(byteBuffer => byteBuffer.arrayBuffer()).foreach(webSocket.send)
      }
      _ <- UIO { webSocket.onopen = (event: Event) => openBus.writer.onNext(event) }
      _ <- UIO {
        webSocket.onerror = (event: Event) => {
          if (scala.scalajs.LinkingInfo.developmentMode) {
            dom.console.error(event)
          }
          errorBus.writer.onNext(event)
        }
      }
      _ <- ZIO.effectTotal {
        webSocket.onclose = (_: Event) => closeBus.writer.onNext(())
      }
    } yield ()

  def open()(using Owner): CancelableFuture[Unit] =
    zio.Runtime.default.unsafeRunToFuture(openWebSocketConnection)

  def close(): Unit = {
    socket.close()
    closeBus.writer.onNext(())
  }

  val $in: EventStream[In]       = inBus.events
  val outWriter: WriteBus[Out]   = outBus.writer
  val $closed: EventStream[Unit] = closeBus.events
  val $error: EventStream[Event] = errorBus.events
  val $open: EventStream[Event]  = openBus.events

}

object BinaryWebSocket {

  def apply[In, Out](path: PathSegment[Unit, _], host: String = dom.document.location.host)(using
      Pickler[In],
      Pickler[Out]
  ): BinaryWebSocket[In, Out] = new BinaryWebSocket(path.createPath(), host)

  def apply[In, Out, Q](
      path: PathSegment[Unit, _],
      query: QueryParameters[Q, _],
      q: Q,
      host: String
  )(using Pickler[In], Pickler[Out]): BinaryWebSocket[In, Out] =
    new BinaryWebSocket((path ? query).createUrlString((), q), host)

  def apply[In, Out, Q](
      path: PathSegment[Unit, _],
      query: QueryParameters[Q, _],
      q: Q
  )(using Pickler[In], Pickler[Out]): BinaryWebSocket[In, Out] = apply(path, query, q, dom.document.location.host)

  given asObserver[Out]: Conversion[BinaryWebSocket[_, Out], Observer[Out]] with
    def apply(socket: BinaryWebSocket[_, Out]): Observer[Out] = socket.outWriter

}
