import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{Circle, ConvexPolygon, Polygon, Shape}
import boopickle.Default.*
import gamelogic.abilities.*
import gamelogic.buffs.godsbuffs.{DamageZoneSpawn, HealUnitDealer}
import gamelogic.buffs.resourcebuffs.EnergyFiller
import gamelogic.entities.{Entity, EntityCastingInfo}
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.*
import gamelogic.buffs.{Buff, PassiveBuff, SimplePassiveBuff, SimpleTickerBuff, TickerBuff}

package object gamecommunication {

  private def longPickler = summon[Pickler[Long]]
  private def intPickler  = summon[Pickler[Int]]

  given Pickler[Entity.Id] = longPickler.xmap(Entity.Id.apply)(_.toLong)

  given Pickler[Ability.UseId] = longPickler.xmap(Ability.UseId.apply)(_.toLong)

  given Pickler[Ability.AbilityId] = intPickler.xmap(i => Ability.allAbilityIds(i - 1))(_.toInt)

  given gameActionIdPickler: Pickler[GameAction.Id] = longPickler.xmap(GameAction.Id.apply)(_.toLong)

  given buffId: Pickler[Buff.Id] = longPickler.xmap(Buff.Id.apply)(_.toLong)

  given Pickler[Circle] = summon[Pickler[Double]].xmap(new Circle(_))(_.radius)

  given Pickler[Polygon] = summon[Pickler[Vector[Complex]]].xmap(Polygon(_))(_.vertices)

  given Pickler[Shape] = compositePickler[Shape]
    .addConcreteType[Circle]
    .addConcreteType[Polygon]
    .addConcreteType[ConvexPolygon]

  given Pickler[SimplePassiveBuff] = compositePickler[SimplePassiveBuff]
    .addConcreteType[Shield]
    .addConcreteType[BulletGlue]

  given Pickler[SimpleTickerBuff] = compositePickler[SimpleTickerBuff]
    .addConcreteType[HealUnitDealer]
    .addConcreteType[DamageZoneSpawn]
    .addConcreteType[EnergyFiller]

  given Pickler[Ability] = compositePickler[Ability]
    .addConcreteType[ActivateShield]
    .addConcreteType[BigBullet]
    .addConcreteType[CraftGunTurret]
    .addConcreteType[CreateBarrier]
    .addConcreteType[CreateBulletAmplifier]
    .addConcreteType[CreateHealingZone]
    .addConcreteType[LaserAbility]
    .addConcreteType[LaunchSmashBullet]
    .addConcreteType[PutBulletGlue]
    .addConcreteType[Teleportation]
    .addConcreteType[TripleBullet]

  given Pickler[Entity] = compositePickler[Entity]
    .addConcreteType[AbilityGiver]
    .addConcreteType[Barrier]
    .addConcreteType[BulletAmplifier]
    .addConcreteType[Bullet]
    .addConcreteType[DamageZone]
    .addConcreteType[God]
    .addConcreteType[GunTurret]
    .addConcreteType[HealUnit]
    .addConcreteType[HealingZone]
    .addConcreteType[LaserLauncher]
    .addConcreteType[Mist]
    .addConcreteType[Obstacle]
    .addConcreteType[Player]
    .addConcreteType[SmashBullet]
    .addConcreteType[TeamFlag]
    .addConcreteType[Zone]

  given Pickler[GameAction] = compositePickler[GameAction]
    .addConcreteType[BulletAmplifierAmplified]
    .addConcreteType[ChangeBulletRadius]
    .addConcreteType[ChangeRemourceAmount]
    .addConcreteType[CreateGod]
    .addConcreteType[DestroyBarrier]
    .addConcreteType[DestroyBulletAmplifier]
    .addConcreteType[DestroyBullet]
    .addConcreteType[DestroyDamageZone]
    .addConcreteType[DestroyGunTurret]
    .addConcreteType[DestroyHealUnit]
    .addConcreteType[DestroyHealingZone]
    .addConcreteType[DestroyLaserLauncher]
    .addConcreteType[DestroySmashBullet]
    .addConcreteType[EntityStartsCasting]
    .addConcreteType[FireLaser]
    .addConcreteType[GameBegins]
    .addConcreteType[GameEnded]
    .addConcreteType[GunTurretShoots]
    .addConcreteType[GunTurretTakesDamage]
    .addConcreteType[HealingZoneHeals]
    .addConcreteType[NewAbilityGiver]
    .addConcreteType[NewBarrier]
    .addConcreteType[NewBulletAmplifier]
    .addConcreteType[NewBullet]
    .addConcreteType[NewGunTurret]
    .addConcreteType[NewHealUnit]
    .addConcreteType[NewHealingZone]
    .addConcreteType[NewLaserLauncher]
    .addConcreteType[NewObstacle]
    .addConcreteType[NewPlayer]
    .addConcreteType[NewSmashBullet]
    .addConcreteType[NewTeamFlag]
    .addConcreteType[PlayerBringsFlagBack]
    .addConcreteType[PlayerDead]
    .addConcreteType[PlayerDropsFlag]
    .addConcreteType[PlayerHitByBullet]
    .addConcreteType[PlayerHitByMultipleBullets]
    .addConcreteType[PlayerHitBySmashBullet]
    .addConcreteType[PlayerTakeAbilityGiver]
    .addConcreteType[PlayerTakeDamage]
    .addConcreteType[PlayerTakeHealUnit]
    .addConcreteType[PlayerTakesFlag]
    .addConcreteType[PutSimplePassiveBuff]
    .addConcreteType[PutSimpleTickerBuff]
    .addConcreteType[RemoveBuff]
    .addConcreteType[RemoveRelevantAbility]
    .addConcreteType[SmashBulletGrows]
    .addConcreteType[TickerBuffTicks]
    .addConcreteType[TranslatePlayer]
    .addConcreteType[UpdateDamageZone]
    .addConcreteType[UpdateHealingZone]
    .addConcreteType[UpdateMist]
    .addConcreteType[UpdatePlayerPos]
    .addConcreteType[UseAbilityAction]

  given Pickler[GameState] = summon[Pickler[
    (
        Long,
        Option[Long],
        Option[Long],
        Polygon,
        Map[Entity.Id, EntityCastingInfo],
        Map[Entity.Id, Map[Buff.Id, SimplePassiveBuff]],
        Map[Entity.Id, Map[Buff.Id, SimpleTickerBuff]],
        Map[Entity.Id, Entity],
        Map[Entity.Id, Player]
    )
  ]].xmap {
    case (time, startTime, endTime, gameBounds, castingEntityInfo, passiveBuff, tickerBuff, entities, deadPlayers) =>
      new GameState(
        time,
        startTime,
        endTime,
        gameBounds,
        castingEntityInfo,
        passiveBuff,
        tickerBuff,
        entities,
        deadPlayers
      )
  }(gameState =>
    (
      gameState.time,
      gameState.startTime,
      gameState.endTime,
      gameState.gameBounds,
      gameState.castingEntityInfo,
      gameState.passiveBuffs.map((entityId, buffs) =>
        entityId -> buffs.collect { case (key, buff: SimplePassiveBuff) => key -> buff }
      ),
      gameState.tickerBuffs.map((entityId: Entity.Id, buffs: Map[Buff.Id, TickerBuff]) =>
        entityId -> buffs.collect { case (key, buff: SimpleTickerBuff) => key -> buff }
      ),
      gameState.entities,
      gameState.deadPlayers
    )
  )

}
