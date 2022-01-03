package models.menus

import io.circe.generic.semiauto._
import io.circe.{Codec, Decoder, Encoder}

final case class PlayerName(name: String) {
  override def toString: String = s"[$name]"
}

object PlayerName {
  implicit val codec: Codec[PlayerName] = Codec.from(
    Decoder[String].map(name => PlayerName(name.slice(1, name.length - 1))),
    Encoder[String].contramap(_.toString)
  )
}
