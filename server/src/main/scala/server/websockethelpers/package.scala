package server

import io.circe.*
import io.circe.parser.decode
import io.circe.syntax.*
import akka.util.{ByteString, Timeout}
import akka.actor.typed.scaladsl.*
import akka.actor.typed.*
import akka.NotUsed
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import boopickle.Default.*
import scala.util.Try

import scala.concurrent.duration.*
import scala.concurrent.Future
import scala.reflect.ClassTag

package object websockethelpers {

  def heartbeat[T](t: T, every: FiniteDuration): Source[T, NotUsed] = Source.repeat(t).throttle(1, every)

  case class PoisonPill()

  /** Creates a [[Flow]] that decouples messages passing through an actor.
    *
    * All entering messages are sent to the actor.
    *
    * The created [[Behavior]] receives as input an [[ActorRef]] to send messages to. These messages are sent downstream
    * through the flow.
    *
    * A [[PoisonPill]] can be sent to that [[ActorRef]] in order to close the flow.
    *
    * @param behavior
    *   [[Behavior]] to handle incoming messages. It receives an actor for sending outgoing messages.
    * @param name
    *   name to give to the actor
    * @param onCompleteMessage
    *   last message sent to complete
    * @param onFailureMessage
    *   message to send when there is a failure.
    */
  def flowThroughActor[In, Out](
      behavior: ActorRef[Out | PoisonPill] => Behavior[In],
      name: String,
      onCompleteMessage: In,
      onFailureMessage: Throwable => In
  )(using
      system: ActorSystem[_],
      ct: ClassTag[Out]
  ): Flow[In, Out, NotUsed] = {

    type FullOut = Out | PoisonPill
    val (outerWorld, source) = ActorSource
      .actorRef[FullOut](
        { case PoisonPill() => },
        { case _ if false => new RuntimeException("meh") },
        30,
        OverflowStrategy.fail
      )
      .toMat(BroadcastHub.sink[FullOut])(Keep.both)
      .run()

    val actor = system.systemActorOf(behavior(outerWorld), name)

    Flow.fromSinkAndSource(
      ActorSink.actorRef(actor, onCompleteMessage, onFailureMessage),
      source.collect { case out: Out => out }
    )
  }

  /** Same as [[webSocketService]], but uses boopickle and binary messages instead. */
  def binaryWebSocketService[In, Out, Mat](
      via: Flow[In, Out, Mat]
  )(using system: ActorSystem[_], decoder: Pickler[In], encoder: Pickler[Out]): Flow[Message, Message, Mat] = {
    import system.executionContext
    Flow[Message]
      .mapAsync(16) {
        case tm: TextMessage =>
          tm.textStream.runWith(Sink.ignore)
          Future.successful(Left(new RuntimeException("Don't handle text messages")))
        case bm: BinaryMessage =>
          bm.dataStream
            .runWith(Sink.reduce(_ ++ _))
            .map(rawContent => Try(Unpickle[In].fromBytes(rawContent.asByteBuffer)).toEither)
      }
      .alsoTo(
        Flow[Either[Throwable, _]]
          .collect { case Left(error) => error }
          .to(Sink.foreach[Throwable](_.printStackTrace()))
      )
      .collect { case Right(in) => in }
      .viaMat(via)(Keep.right)
      .map(out => BinaryMessage(ByteString.fromByteBuffer(Pickle.intoBytes(out))))
  }

  /** Flow used for the web socket route. Incoming string messages are deserialized into instances of In using Circe,
    * and output Out messages are serialized back into string message using Circe again.
    *
    * Bytes should be treated differently, although the idea would be the same...
    */
  def webSocketService[In, Out, Mat](via: Flow[In, Out, Mat])(implicit
      as: ActorSystem[_],
      decoder: Decoder[In],
      encoder: Encoder[Out]
  ): Flow[Message, Message, Mat] = {
    import as.executionContext
    Flow[Message]
      .mapAsync(16) {
        case tm: TextMessage =>
          tm.textStream.runWith(Sink.reduce(_ ++ _)).map(msg => decode[In](msg))
        case bm: BinaryMessage =>
          bm.dataStream.runWith(Sink.ignore)
          Future.successful(Left(new RuntimeException("Don't handle binary messages")))
      }
      .alsoTo(
        Flow[Either[Throwable, _]]
          .collect { case Left(error) => error }
          .to(Sink.foreach[Throwable](_.printStackTrace()))
      )
      .collect { case Right(in) => in }
      .viaMat(via)(Keep.right)
      .map(out => TextMessage(encoder(out).noSpaces))
  }

}
