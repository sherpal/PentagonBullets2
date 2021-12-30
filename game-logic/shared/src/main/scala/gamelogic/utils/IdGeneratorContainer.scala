package gamelogic.utils

import gamelogic.abilities.Ability
import gamelogic.entities.Entity
import gamelogic.gamestate.GameAction

/** Contains all the generators that are used for the different kind of ids during the game.
  */
final case class IdGeneratorContainer(
    entityIdGenerator: EntityIdGenerator,
    gameActionIdGenerator: GameActionIdGenerator,
    abilityUseIdGenerator: AbilityUseIdGenerator
)

object IdGeneratorContainer {

  def initialIdGeneratorContainer: IdGeneratorContainer = IdGeneratorContainer(
    new EntityIdGenerator(Entity.Id.initial),
    new GameActionIdGenerator(GameAction.Id.initial),
    new AbilityUseIdGenerator(Ability.UseId.initial)
  )

}