package game

import com.raquo.laminar.api.A.*
import gamecommunication.ClientToServer
import gamelogic.entities.*
import gamelogic.gamestate.{GameAction, GameState}
import be.doeraene.physics.Complex
import gamelogic.abilities.Ability
import game.ui.GameDrawer
import gamelogic.entities.ActionSource.PlayerSource
import gamelogic.entities.concreteentities.Player
import models.playing.UserInput
import gamelogic.gamestate.gameactions.*
import gamelogic.utils.IdGeneratorContainer
import gamelogic.abilities.*
import gamelogic.abilities.Ability.*

/** Singleton adding the effect of casting abilities.
  */
final class CastAbilitiesHandler(
    playerId: Entity.Id,
    userControls: UserControls,
    $gameStates: Signal[GameState],
    $gameMousePosition: Signal[Complex],
    socketOutWriter: Observer[ClientToServer],
    useAbilityEvents: EventStream[Ability.AbilityId],
    gameDrawer: GameDrawer,
    deltaTimeWithServer: Long,
    currentTime: () => Long
)(implicit owner: Owner) {
  private def serverTime = currentTime()

  private val dummyUseId: UseId = UseId.initial

  /** We need one to generate dummy ids. */
  private implicit val idGeneratorContainer: IdGeneratorContainer = IdGeneratorContainer.initialIdGeneratorContainer

  private def sendAction(action: GameAction): Unit =
    socketOutWriter.onNext(ClientToServer.GameActionWrapper(action :: Nil))

  private def sendUseAbility(ability: Ability): Unit =
    sendAction(UseAbilityAction(GameAction.Id.initial, serverTime, ability, Ability.UseId.initial, PlayerSource))

  private case class AbilityMaker[+A <: Ability](
      abilityId: AbilityIdFor[A],
      fn: (GameState, Complex, Player) => Option[A]
  )

  private val allAbilityMakers: List[AbilityMaker[Ability]] = List(
    AbilityMaker(
      activateShieldId,
      (gameState, worldMousePos, player) => Some(ActivateShield(serverTime, dummyUseId, playerId))
    ),
    AbilityMaker(
      bigBulletId,
      { (gameState, worldMousePos, player) =>
        val rotation    = (worldMousePos - player.pos).arg
        val startingPos = player.pos + player.shape.radius * Complex.rotation(rotation)
        Some(BigBullet(serverTime, dummyUseId, player.id, player.team, startingPos, rotation))
      }
    ),
    AbilityMaker(
      craftGunTurretId,
      (gameState, worldMousePos, player) =>
        Some(CraftGunTurret(serverTime, dummyUseId, player.id, player.team, player.pos))
    ),
    AbilityMaker(
      createBarrierId,
      (gameState, worldMousePos, player) => {
        val time     = serverTime
        val rotation = (worldMousePos - player.pos).arg
        val ability  = CreateBarrier(time, dummyUseId, playerId, player.team, worldMousePos, rotation)
        Option.when(ability.isLegal(gameState, time))(ability)
      }
    ),
    AbilityMaker(
      createBulletAmplifierId,
      (gameState, worldMousePos, player) => ???
    ),
    AbilityMaker(
      createHealingZoneId,
      (gameState, worldMousePos, player) => ???
    ),
    AbilityMaker(
      laserId,
      (gameState, worldMousePos, player) => ???
    ),
    AbilityMaker(
      launchSmashBulletId,
      (gameState, worldMousePos, player) => ???
    ),
    AbilityMaker(
      putBulletGlueId,
      (gameState, worldMousePos, player) => ???
    ),
    AbilityMaker(
      teleportationId,
      (gameState, worldMousePos, player) => ???
    ),
    AbilityMaker(
      tripleBulletId,
      (gameState, worldMousePos, player) => ???
    )
  )

  private val abilityMakerFromAbilityId: Map[Ability.AbilityId, (GameState, Complex, Player) => Option[Ability]] =
    allAbilityMakers.map(maker => maker.abilityId -> maker.fn).toMap

  EventStream
    .merge(
      userControls.downInputs
        .collect { case abilityInput: UserInput.AbilityInput => abilityInput }
        .withCurrentValueOf($gameStates)
        .map((input, gameState) => (input, gameState, gameState.players.get(playerId)))
        .collect { case (input, gameState, Some(me)) => (input, gameState, me) }
        .withCurrentValueOf($gameMousePosition)
        .map { case (abilityInput, gameState, me, worldMousePos) =>
          (gameState, abilityInput.abilityId(me), worldMousePos, me)
        }
        .collect { case (gameState, Some(abilityId), worldMousePos, me) => (gameState, abilityId, worldMousePos, me) },
      useAbilityEvents
        .withCurrentValueOf($gameStates)
        .map((abilityId, gameState) => (abilityId, gameState, gameState.players.get(playerId)))
        .collect { case (abilityId, gameState, Some(me)) => (gameState, abilityId, me) }
        .withCurrentValueOf($gameMousePosition)
    )
    .filter(_._1.isPlaying)
    .foreach { case (gameState: GameState, abilityId: Ability.AbilityId, worldMousePos: Complex, me: Player) =>
      val maybeAbility: Option[Ability] = abilityMakerFromAbilityId.get(abilityId) match {
        case Some(fn) => fn(gameState, worldMousePos, me)
        case None     => throw new RuntimeException(s"This ability Id ($abilityId) is not supported yet.")
      }

      maybeAbility.filter(_.isUp(me, serverTime, 1000)).foreach(sendUseAbility)
    }

}
