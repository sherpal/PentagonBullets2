package models

import io.circe.Decoder

package object menus {
  def valueDecoder[T](t: T): Decoder[T] = Decoder[String].emapTry {
    case s if s == t.toString => scala.util.Success(t)
    case s => scala.util.Failure(new RuntimeException(s"Decoded string value $s was not equal to $t"))
  }

  extension [T](decoder: Decoder[T]) def widen[U](using ev: T <:< U): Decoder[U] = decoder.map(ev.apply)
}
