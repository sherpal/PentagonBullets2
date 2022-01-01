package models.gamecodecs

import gamelogic.abilities.Ability
import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto.*

object CirceCodecs {

  implicit val abilityIdCodec: Codec[Ability.AbilityId] = Codec
    .from(Decoder[Int], Encoder[Int])
    .iemap(int =>
      Ability.AbilityId
        .fromInt(int)
        .toRight(
          s"Ability id $int is out of range [0, ${Ability.maxAbilityId}]."
        )
    )(_.toInt)
}
