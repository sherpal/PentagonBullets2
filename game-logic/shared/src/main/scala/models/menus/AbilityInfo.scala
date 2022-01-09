package models.menus

import gamelogic.abilities.Ability
import gamelogic.abilities.Ability.*
import gamelogic.entities.concreteentities.HealingZone

final case class AbilityInfo(id: Ability.AbilityId, name: String, description: String)

object AbilityInfo {

  val allAbilityInfo: List[AbilityInfo] = List(
    AbilityInfo(
      bigBulletId,
      "Big Bullet",
      "Shoots a big bullet towards your mouse's position, that goes twice as fast and deals three times as much damage as a normal bullet."
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
    AbilityInfo(
      createHealingZoneId,
      "Healing Zone",
      s"Place a healing zone at your mouse's position. This healing zone heals you (or any member of your team) " +
        s"periodically (every ${HealingZone.tickRate} ms), for a maximum total of ${HealingZone.lifeSupply} life " +
        s"points. The healing zone vanishes after ${HealingZone.lifetime} seconds if it was not consumed by then."
    ),
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
      putBulletGlueId,
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

  lazy val abilityInfoById: Map[Ability.AbilityId, AbilityInfo] = allAbilityInfo.map(info => info.id -> info).toMap

  def abilityInfoFromId(id: Ability.AbilityId): AbilityInfo =
    abilityInfoById(id) // call is safe because AbilityIds can't be created.

}
