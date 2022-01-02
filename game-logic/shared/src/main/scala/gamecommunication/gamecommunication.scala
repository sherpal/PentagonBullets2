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
    .addConcreteType[LaunchSmashBullet]
    .addConcreteType[CreateBulletAmplifier]
    .addConcreteType[CreateHealingZone]
    .addConcreteType[LaserAbility]
    .addConcreteType[BigBullet]
    .addConcreteType[CreateBarrier]
    .addConcreteType[TripleBullet]
    .addConcreteType[ActivateShield]
    .addConcreteType[CraftGunTurret]
    .addConcreteType[Teleportation]
    .addConcreteType[PutBulletGlue]

  given Pickler[Entity] = compositePickler[Entity]
    .addConcreteType[GunTurret]
    .addConcreteType[HealingZone]
    .addConcreteType[BulletAmplifier]
    .addConcreteType[Barrier]
    .addConcreteType[Bullet]
    .addConcreteType[HealUnit]
    .addConcreteType[AbilityGiver]
    .addConcreteType[TeamFlag]
    .addConcreteType[Zone]
    .addConcreteType[Mist]
    .addConcreteType[SmashBullet]
    .addConcreteType[Player]
    .addConcreteType[DamageZone]
    .addConcreteType[Obstacle]
    .addConcreteType[LaserLauncher]

  given Pickler[GameAction] = compositePickler[GameAction]
    .addConcreteType[DestroySmashBullet]
    .addConcreteType[FireLaser]
    .addConcreteType[NewHealUnit]
    .addConcreteType[NewAbilityGiver]
    .addConcreteType[PlayerTakesFlag]
    .addConcreteType[SmashBulletGrows]
    .addConcreteType[NewTeamFlag]
    .addConcreteType[PlayerHitByBullet]
    .addConcreteType[HealingZoneHeals]
    .addConcreteType[ChangeBulletRadius]
    .addConcreteType[BulletAmplifierAmplified]
    .addConcreteType[NewPlayer]
    .addConcreteType[NewHealingZone]
    .addConcreteType[PlayerTakeAbilityGiver]
    .addConcreteType[PlayerHitBySmashBullet]
    .addConcreteType[DestroyGunTurret]
    .addConcreteType[UpdateHealingZone]
    .addConcreteType[NewBarrier]
    .addConcreteType[DestroyLaserLauncher]
    .addConcreteType[UseAbilityAction]
    .addConcreteType[PlayerHitByMultipleBullets]
    .addConcreteType[NewLaserLauncher]
    .addConcreteType[NewObstacle]
    .addConcreteType[PlayerTakeHealUnit]
    .addConcreteType[GameBegins]
    .addConcreteType[NewSmashBullet]
    .addConcreteType[NewBulletAmplifier]
    .addConcreteType[TranslatePlayer]
    .addConcreteType[UpdatePlayerPos]
    .addConcreteType[UpdateDamageZone]
    .addConcreteType[DestroyHealUnit]
    .addConcreteType[PlayerDead]
    .addConcreteType[DestroyBarrier]
    .addConcreteType[GameEnded]
    .addConcreteType[DestroyBullet]
    .addConcreteType[DestroyBulletAmplifier]
    .addConcreteType[DestroyHealingZone]
    .addConcreteType[UpdateMist]
    .addConcreteType[GunTurretTakesDamage]
    .addConcreteType[RemoveRelevantAbility]
    .addConcreteType[DestroyDamageZone]
    .addConcreteType[PlayerBringsFlagBack]
    .addConcreteType[PlayerDropsFlag]
    .addConcreteType[GunTurretShoots]
    .addConcreteType[PlayerTakeDamage]
    .addConcreteType[NewGunTurret]
    .addConcreteType[NewBullet]
    .addConcreteType[PutSimplePassiveBuff]

}
