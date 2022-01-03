package models.playing

import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.concreteentities.{GameArea, Player}
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer
import models.menus.GameJoinedInfo

object GameStateInitializer {

  def initializeGameState(
      gameInfo: GameJoinedInfo
  )(implicit idGeneratorContainer: IdGeneratorContainer): (GameState, List[GameAction]) = {
    val players         = gameInfo.players
    val numberOfPlayers = gameInfo.numberOfPlayers

    val initialGameState = GameState.initialGameState

    val gameArea = GameArea(gameInfo.numberOfPlayers)

    val centerOctagonRadius: Double = 50.0 * math.sqrt(2)

    val createCenterSquare = gameArea.createCenterSquare(centerOctagonRadius, ServerSource)

    val newPlayerActions = players.values.toList.map(gameArea.createPlayer)

    val gameStateAfterNewPlayers: GameState = createCenterSquare(initialGameState).applyActions(newPlayerActions)

    val playerTranslations = gameArea.translateTeams(gameStateAfterNewPlayers, Player.radius * 5)

    val gameStateAfterPlayers = gameStateAfterNewPlayers.applyActions(playerTranslations)

    val (gameStateAfterObstacles, newObstaclesActions) = (1 until 3 * (numberOfPlayers - 1)).foldLeft(
      (gameStateAfterPlayers, List.empty[GameAction])
    ) { case ((gameStateAcc, actionsAcc), _) =>
      val action = gameArea.createObstacle(gameStateAcc, 50, 50, ServerSource)

      (action(gameStateAcc), action +: actionsAcc)
    }

    (initialGameState, List(createCenterSquare) ++ newPlayerActions ++ playerTranslations ++ newObstaclesActions)
  }

}
