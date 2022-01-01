package actors

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

import scala.reflect.ClassTag

object Implicits {
  private object PoisonPill

  extension [T](ref: ActorRef[T])
    def contramap[U](f: U => T)(using context: ActorContext[_], ct: ClassTag[U]): ActorRef[U] = context.spawn(
      Behaviors.setup[U | PoisonPill.type] { adapterContext =>
        adapterContext.watchWith(ref, PoisonPill)
        Behaviors.receiveMessage[U | PoisonPill.type] {
          case u: U =>
            ref ! f(u)
            Behaviors.same
          case _ => Behaviors.stopped
        }
      },
      "Adapter" ++ java.util.UUID.randomUUID().toString
    )
}
