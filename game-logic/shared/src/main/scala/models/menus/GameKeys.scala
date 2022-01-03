package models.menus

import io.circe.Codec

object GameKeys {

  opaque type GameKey = java.util.UUID

  object GameKey {
    def random(): GameKey = java.util.UUID.randomUUID()

    def fromString(string: String): Either[Throwable, GameKey] =
      scala.util.Try(java.util.UUID.fromString(string)).toEither

    given Codec[GameKey] = Codec[java.util.UUID]
  }

}
