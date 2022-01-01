package models.menus

import io.circe.generic.semiauto._
import io.circe.Codec

final case class PlayerName(name: String)

object PlayerName {
  implicit val codec: Codec[PlayerName] = deriveCodec
}
