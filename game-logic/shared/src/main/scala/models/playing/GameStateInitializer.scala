package models.playing

import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.concreteentities.{GameArea, God, Player}
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.utils.IdGeneratorContainer
import models.menus.GameJoinedInfo
import _root_.utils.misc.RGBColour
import gamelogic.buffs.godsbuffs.{DamageZoneSpawn, HealUnitDealer}
import gamelogic.entities.Entity
import gamelogic.gamestate.gameactions.{CreateGod, PutSimpleTickerBuff, UpdateMist}
import gamelogic.utils.Time
import gamelogic.buffs.Buff

object GameStateInitializer {

  def initializeGameState(
      gameInfo: GameJoinedInfo
  )(implicit idGeneratorContainer: IdGeneratorContainer): (GameState, List[GameAction]) = {
    val players         = gameInfo.players
    val numberOfPlayers = gameInfo.numberOfPlayers

    val initialGameState = GameState.initialGameState

    val createGod = CreateGod(GameAction.newId(), Time.currentTime(), Entity.newId())

    val healUnitDealerCreation = PutSimpleTickerBuff(
      GameAction.newId(),
      Time.currentTime(),
      HealUnitDealer(Buff.nextBuffId(), createGod.godId, Time.currentTime(), Time.currentTime()),
      ServerSource
    )

    val damageZoneSpawnCreation = PutSimpleTickerBuff(
      GameAction.newId(),
      Time.currentTime(),
      DamageZoneSpawn(Buff.nextBuffId(), createGod.godId, Time.currentTime(), Time.currentTime()),
      ServerSource
    )

    val godActions = List(createGod, healUnitDealerCreation, damageZoneSpawnCreation)

    val gameArea = GameArea(gameInfo.numberOfPlayers)

    val createMists = List(
      UpdateMist(
        GameAction.newId(),
        Time.currentTime(),
        Entity.newId(),
        Time.currentTime() + 5000,
        Time.currentTime() + 5000,
        gameArea.gameAreaSideLength - 1, // -1 otherwise empty polygon make the server crash
        gameArea.gameAreaSideLength,
        ServerSource
      )
    ).filter(_ => gameInfo.players.size > 2)

    val centerOctagonRadius: Double = 50.0 * math.sqrt(2)

    val createCenterSquare     = gameArea.createCenterSquare(centerOctagonRadius, ServerSource)
    val createObstaclesActions = createCenterSquare +: gameArea.createGameBoundsBarriers

    val newPlayerActions = players.values.zip(RGBColour.coloursForPlayers).toList.map(gameArea.createPlayer(_, _))

    val gameStateAfterNewPlayers: GameState =
      initialGameState.applyActions(godActions ++ createMists ++ createObstaclesActions ++ newPlayerActions)

    assert(
      gameStateAfterNewPlayers
        .allTEntities[God]
        .values
        .exists(god => gameStateAfterNewPlayers.tickerBuffs.get(god.id).exists(_.size == 2)),
      gameStateAfterNewPlayers
        .allTEntities[God]
        .values
        .toList
        .map(god => gameStateAfterNewPlayers.tickerBuffs.get(god.id))
        .toString()
    )

    val playerTranslations = gameArea.translateTeams(gameStateAfterNewPlayers, Player.radius * 5)

    val gameStateAfterPlayers = gameStateAfterNewPlayers.applyActions(playerTranslations)

    val (gameStateAfterObstacles, newObstaclesActions) = (1 until 3 * (numberOfPlayers - 1)).foldLeft(
      (gameStateAfterPlayers, List.empty[GameAction])
    ) { case ((gameStateAcc, actionsAcc), _) =>
      val action = gameArea.createObstacle(gameStateAcc, 100, 100, ServerSource)

      (action(gameStateAcc), action +: actionsAcc)
    }

    (
      initialGameState,
      godActions ++ createMists ++ createObstaclesActions ++ newPlayerActions ++ playerTranslations ++ newObstaclesActions
    )
  }

}
