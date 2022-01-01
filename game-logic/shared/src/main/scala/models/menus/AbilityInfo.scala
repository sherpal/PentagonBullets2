package models.menus

import gamelogic.abilities.Ability
import gamelogic.abilities.Ability.*

final case class AbilityInfo(id: Ability.AbilityId, name: String, description: String)

object AbilityInfo {

  /** val craftGunTurretId: AbilityId = nextAbilityId() val createBarrierId: AbilityId = nextAbilityId() val
    * createBulletAmplifierId: AbilityId = nextAbilityId() val createHealingZoneId: AbilityId = nextAbilityId() val
    * laserId: AbilityId = nextAbilityId() val launchSmashBulletId: AbilityId = nextAbilityId() val putBulletGlue:
    * AbilityId = nextAbilityId() val tripleBulletId: AbilityId = nextAbilityId() val teleportationId: AbilityId =
    * nextAbilityId()
    */
  val allAbilityInfo: List[AbilityInfo] = List(
    AbilityInfo(
      bigBulletId,
      "Big Bullet",
      "Shoots a big bullet to your enemy, that goes twice as fast and deals three times as much damage as a normal bullet."
    ),
    AbilityInfo(
      craftGunTurretId,
      "Gun Turret",
      "Creates a Gun Turret at your position. A Gun Turret constantly fires towards the closest enemy (if within range)."
    ),
    AbilityInfo(
      createBarrierId,
      "Barrier",
      "Place a barrier at your mouse's position. This barrier acts as an obstacle for all your opponents."
    ),
    AbilityInfo(createBulletAmplifierId, "Bullet Amplifier", "Don't use it. It sucks."),
    AbilityInfo(createHealingZoneId, "Healing Zone", "todo"),
    AbilityInfo(
      laserId,
      "Laser",
      "First step: place a laser launcher at your position. Second step: fire a laser between the fire launcher and you, dealing 30 damages to any enemy within."
    ),
    AbilityInfo(
      launchSmashBulletId,
      "Smash Bullet",
      "Launch a smash bullet towards your mouse's position. This bullet has a shorter range as a normal bullet, but it grows overtime, passes through walls and if it hits an enemy, it removes half their remaining life."
    ),
    AbilityInfo(
      putBulletGlue,
      "Bullet Glue",
      "Slows down all existing enemy bullets, as well as all created bullets for the next 5 seconds, by reducing their speed by 2."
    ),
    AbilityInfo(
      tripleBulletId,
      "Penta Bullet",
      "Shoots 5 bullets in a pi / 8 arc towards the mouse. They go 5/4 times the speed of a normal bullet."
    ),
    AbilityInfo(teleportationId, "Teleportation", "Teleports yourself instantly to your mouse's position.")
  ).sortBy(_.name)

  def abilityInfoFromId(id: Ability.AbilityId): AbilityInfo =
    allAbilityInfo.find(_.id == id).get // get is safe because AbilityIds can't be created.

}
