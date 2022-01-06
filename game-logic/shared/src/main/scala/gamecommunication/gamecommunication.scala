import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{Circle, Polygon, Shape}
import boopickle.Default.*
import gamelogic.abilities.*
import gamelogic.entities.Entity
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.GameAction
import gamelogic.gamestate.gameactions.*
import gamelogic.buffs.{Buff, SimplePassiveBuff}

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

  given Pickler[SimplePassiveBuff] = compositePickler[SimplePassiveBuff]
    .addConcreteType[Shield]
    .addConcreteType[BulletGlue]

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
    .addConcreteType[RemoveRelevantAbility]
    .addConcreteType[SmashBulletGrows]
    .addConcreteType[TranslatePlayer]
    .addConcreteType[UpdateDamageZone]
    .addConcreteType[UpdateHealingZone]
    .addConcreteType[UpdateMist]
    .addConcreteType[UpdatePlayerPos]
    .addConcreteType[UseAbilityAction]

}
