package gamelogic.gamestate.serveractions
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.Entity
import gamelogic.gamestate.{ActionGatherer, GameAction}
import gamelogic.gamestate.gameactions.{NewAbilityGiver, PlayerDead}
import gamelogic.utils.IdGeneratorContainer

final class ManageDeadPlayers(popAbilityGivers: Boolean) extends ServerActionFromActionList {

  def createActionList(currentState: ActionGatherer, nowGenerator: () => Long)(implicit idGeneratorContainer: IdGeneratorContainer): Iterable[GameAction] = {
    val now = nowGenerator()

    val gameState = currentState.currentGameState

    val deadPlayersActions = gameState.players.values
      .filter(_.lifeTotal <= 0)
      .map(player => PlayerDead(GameAction.newId(), now, player.id, player.name, ServerSource))

    val abilityGiversPops = if popAbilityGivers then (
      for {
        deadPlayerAction <- deadPlayersActions
        player <- gameState.playerById(deadPlayerAction.playerId)
        ability <- player.allowedAbilities.headOption
      } yield NewAbilityGiver(GameAction.newId(), now, Entity.newId(), player.pos, ability, ServerSource)
    ) else Iterable.empty[GameAction]

    deadPlayersActions ++ abilityGiversPops
  }

}
