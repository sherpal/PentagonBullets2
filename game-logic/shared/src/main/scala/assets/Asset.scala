package assets

import gamelogic.abilities.Ability
import gamelogic.buffs.Buff
import urldsl.language.PathSegment
import urldsl.language.dummyErrorImpl.*

import scala.language.implicitConversions
import scala.util.Try

sealed trait Asset {
  val name: String

  override final def equals(obj: Any): Boolean = obj match {
    case that: Asset => this.name == that.name
    case _           => false
  }

  override final def hashCode(): Int = name.hashCode()
}

//noinspection TypeAnnotation
object Asset {

  def fromString(str: String): Asset = new Asset {
    val name: String = str
  }

  def apply(path: PathSegment[Unit, _]): Asset = fromString((root / "assets" / path).createPath())

  final class AssetNotProperlyDefined(asset: Asset)
      extends Exception(
        s"The asset `${asset.toString}` does not seem to work. Did you forget to restart the Dev server? Or Perhaps" +
          s"you forgot to add the asset in resource path? A typo in the filepath could also cause that."
      )

  private implicit class PngString(str: String) {
    def png: String = str ++ ".png"
  }

  object ingame {

    object abilities {
      private val abs = root / "abilities"

      val abilityOverlay  = Asset(abs / "ability-overlay".png)
      val barrier         = Asset(abs / "barrier".png)
      val bigBullet       = Asset(abs / "big_bullet".png)
      val bulletAmplifier = Asset(abs / "bullet_amplifier".png)
      val bulletGlue      = Asset(abs / "bullet_glue".png)
      val gunTurret       = Asset(abs / "gun_turret".png)
      val healingZone     = Asset(abs / "healing_zone".png)
      val laser           = Asset(abs / "laser".png)
      val quadBullets     = Asset(abs / "quad_bullets".png)
      val shield          = Asset(abs / "shield".png)
      val smashBullet     = Asset(abs / "smash_bullet".png)
      val teleportation   = Asset(abs / "teleportation".png)

    }

    object entities {
      private val ent = root / "entities"

      val bulletAmplifier = Asset(ent / "bullet_amplifier".png)
      val healingZone     = Asset(ent / "healing_zone".png)
      val teamFlag        = Asset(ent / "team_flag".png)
    }

    object ui {
      private val ui = root / "ui"

      val abilityFocus     = Asset(ui / "ability_focus".png)
      val playerItemBullet = Asset(ui / "player_item_bullet".png)

      object bars {
        private val bars = ui / "bars"

        val minimalistBar = Asset(bars / "life-bar_wenakari".png)
      }
    }

  }

  implicit def assetAsString(asset: Asset): String = asset.name

  val abilityAssetMap: Map[Ability.AbilityId, Asset] = {
    import ingame.abilities._
    Map(
      Ability.activateShieldId        -> shield,
      Ability.bigBulletId             -> bigBullet,
      Ability.craftGunTurretId        -> gunTurret,
      Ability.createBarrierId         -> barrier,
      Ability.createBulletAmplifierId -> bulletAmplifier,
      Ability.createHealingZoneId     -> healingZone,
      Ability.laserId                 -> laser,
      Ability.launchSmashBulletId     -> smashBullet,
      Ability.putBulletGlueId         -> bulletGlue,
      Ability.tripleBulletId          -> quadBullets,
      Ability.teleportationId         -> teleportation
    )
  }

  val buffAssetMap: Map[Buff.ResourceIdentifier, Asset] = Map()

  val units: List[Asset] = {
    import ingame.entities._
    List(healingZone, bulletAmplifier, teamFlag)
  }

  val misc: List[Asset] = List(
    ingame.abilities.abilityOverlay,
    ingame.ui.abilityFocus,
    ingame.ui.playerItemBullet,
    ingame.ui.bars.minimalistBar
  )

}
