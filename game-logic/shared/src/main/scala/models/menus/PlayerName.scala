package models.menus

import io.circe.generic.semiauto._
import io.circe.Codec

final case class PlayerName(name: String) {
  override def toString: String = s"[$name]"
}

object PlayerName {
  implicit val codec: Codec[PlayerName] = deriveCodec
}
