package game

import com.raquo.laminar.api.A.*
import gamecommunication.ClientToServer
import gamelogic.entities.*
import gamelogic.gamestate.{GameAction, GameState}
import be.doeraene.physics.Complex
import gamelogic.abilities.*
import game.ui.GameDrawer
import gamelogic.entities.ActionSource.PlayerSource
import gamelogic.entities.concreteentities.{Bullet, Player}
import models.playing.UserInput
import gamelogic.gamestate.gameactions.*
import gamelogic.utils.IdGeneratorContainer
import gamelogic.abilities.Ability.*
import gamelogic.entities.Resource.{Energy, ResourceAmount}

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
    currentTime: () => Long,
    setUnconfirmedActions: List[GameAction] => Unit
)(implicit owner: Owner) {
  private def serverTime = currentTime()

  private val dummyUseId: UseId = UseId.initial

  /** We need one to generate dummy ids. */
  private implicit val idGeneratorContainer: IdGeneratorContainer = IdGeneratorContainer.initialIdGeneratorContainer

  private def sendAction(action: GameAction): Unit =
    socketOutWriter.onNext(ClientToServer.GameActionWrapper(action :: Nil))

  private def sendUseAbility(ability: Ability): Unit =
    sendAction(EntityStartsCasting(GameAction.newId(), serverTime, ability.castingTime, ability))

  private case class AbilityMaker[+A <: Ability](
      abilityId: AbilityIdFor[A],
      fn: (GameState, Complex, Player) => Option[A]
  )

  /** Below, the `targetPos` argument is the position of the mouse in the world */
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
      (gameState, targetPos, player) => {
        val time    = serverTime
        val ability = CreateHealingZone(time, dummyUseId, player.id, targetPos)
        Option.when(ability.isLegal(gameState, time))(ability)
      }
    ),
    AbilityMaker(
      laserId,
      (gameState, targetPos, player) => {
        val time       = serverTime
        val stepNumber = LaserAbility.stepFromGameState(gameState, player.id)

        val ability = LaserAbility(time, dummyUseId, player.id, player.team, stepNumber, targetPos)
        Option.when(ability.isLegal(gameState, time))(ability)
      }
    ),
    AbilityMaker(
      launchSmashBulletId,
      (gameState, targetPos, player) => {
        val rotation    = (targetPos - player.pos).arg
        val startingPos = player.pos + Player.radius * Complex.rotation(rotation)
        val time        = serverTime
        val ability     = LaunchSmashBullet(time, dummyUseId, player.id, startingPos, rotation)
        Option.when(ability.isLegal(gameState, time))(ability)
      }
    ),
    AbilityMaker(
      putBulletGlueId,
      (gameState, worldMousePos, player) => {
        val time    = serverTime
        val ability = PutBulletGlue(time, dummyUseId, player.id, player.team)
        Option.when(ability.isLegal(gameState, time))(ability)
      }
    ),
    AbilityMaker(
      teleportationId,
      (gameState, targetPos, player) => {
        val time = serverTime

        val ability = Teleportation(time, dummyUseId, playerId, player.pos, targetPos)

        ability.canBeCast(gameState, time).foreach(message => org.scalajs.dom.console.error(message))

        Option.when(ability.isLegal(gameState, time) && ability.isUp(player, time)) {
          setUnconfirmedActions(ability.createActions(gameState))
          ability
        }
      }
    ),
    AbilityMaker(
      tripleBulletId,
      (gameState, targetPos, player) => {
        val time        = serverTime
        val rotation    = (targetPos - player.pos).arg
        val startingPos = player.pos + Player.radius * Complex.rotation(rotation)

        val ability = TripleBullet(time, dummyUseId, player.id, player.team, startingPos, rotation)
        Option.when(ability.isLegal(gameState, time))(ability)
      }
    )
  )

  private val abilityMakerFromAbilityId: Map[Ability.AbilityId, (GameState, Complex, Player) => Option[Ability]] =
    allAbilityMakers.map(maker => maker.abilityId -> maker.fn).toMap

  EventStream
    .merge(
      userControls.downInputs
        .collect { case UserInput.ShieldAbility => Ability.activateShieldId }
        .withCurrentValueOf($gameStates)
        .map((abilityId, gameState) => (abilityId, gameState, gameState.playerById(playerId)))
        .collect { case (abilityId, gameState, Some(me)) => (gameState, abilityId, me) }
        .withCurrentValueOf($gameMousePosition),
      userControls.downInputs
        .collect { case abilityInput: UserInput.AbilityInput => abilityInput }
        .withCurrentValueOf($gameStates)
        .map((input, gameState) => (input, gameState, gameState.players.get(playerId)))
        .collect { case (input, gameState, Some(me)) => (input, gameState, me) }
        .withCurrentValueOf($gameMousePosition)
        .map { case (abilityInput, gameState, me, worldMousePos) =>
          (gameState, abilityInput.abilityId(me), worldMousePos, me)
        }
        .collect { case (gameState, Some(abilityId), worldMousePos, me) => (gameState, abilityId, me, worldMousePos) },
      useAbilityEvents
        .withCurrentValueOf($gameStates)
        .map((abilityId, gameState) => (abilityId, gameState, gameState.playerById(playerId)))
        .collect { case (abilityId, gameState, Some(me)) => (gameState, abilityId, me) }
        .withCurrentValueOf($gameMousePosition)
    )
    .filter(_._1.isPlaying)
    .foreach { case (gameState: GameState, abilityId: Ability.AbilityId, me: Player, worldMousePos: Complex) =>
      val maybeAbility: Option[Ability] = abilityMakerFromAbilityId.get(abilityId) match {
        case Some(fn) => fn(gameState, worldMousePos, me)
        case None     => throw new RuntimeException(s"This ability Id ($abilityId) is not supported yet.")
      }

      maybeAbility.filter(_.isUp(me, serverTime, 1000)).foreach(sendUseAbility)
    }

  EventStream
    .periodic(158)
    .sample(userControls.$pressedUserInput)
    .filter(_ contains UserInput.DefaultBullets)
    //userControls.downInputs
    //.collect { case UserInput.DefaultBullets => () }
    .sample($gameStates, $gameMousePosition)
    .map((gameState, worldMousePos) => (gameState, gameState.playerById(playerId), worldMousePos))
    .collect { case (gameState, Some(me), worldMousePos) => (gameState, me, worldMousePos) }
    .filter((_, me, _) => me.energy >= NewBullet.bulletPrice)
    .foreach { (gameState: GameState, me: Player, worldMousePos: Complex) =>
      val direction = (worldMousePos - me.pos).normalized
      socketOutWriter.onNext(
        ClientToServer.GameActionWrapper(
          List(
            ChangeRemourceAmount(
              GameAction.newId(),
              serverTime,
              playerId,
              ResourceAmount(-NewBullet.bulletPrice, Energy)
            ),
            NewBullet(
              GameAction.newId(),
              Entity.newId(),
              playerId,
              me.team,
              me.pos + me.shape.radius * direction,
              Bullet.defaultRadius,
              direction.arg,
              Bullet.speed,
              serverTime,
              0,
              PlayerSource
            )
          )
        )
      )
    }

}
