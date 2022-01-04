package game.ui

import game.Camera
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import gamelogic.entities.*
import gamelogic.entities.concreteentities.*
import gamelogic.gamestate.GameState
import typings.pixiJs.PIXI.Texture
import typings.pixiJs.mod.Application
import typings.pixiJs.mod.{Graphics, Sprite}
import utils.misc.RGBColour
import gamelogic.abilities.Ability

import scala.collection.mutable

trait AbilityGiverDrawer extends Drawer {
  def abilityGiverContainer: ReactiveContainer
  def camera: Camera

  def abilityImagesTextures(abilityId: Ability.AbilityId): Texture

  @inline private def abilityGiverStage = abilityGiverContainer.ref

  private def newAbilityGiver(abilityId: Ability.AbilityId): (Sprite, typings.pixiJs.PIXI.Graphics) = {
    val mask = new Graphics()
      .beginFill(0xffffff, 1.0)
      .drawCircle(0, 0, AbilityGiver.radius)
      .endFill()

    val sprite = new Sprite(abilityImagesTextures(abilityId))

    sprite.mask = mask

    (sprite, mask)
  }

  private val abilityGivers: mutable.Map[Entity.Id, (Sprite, typings.pixiJs.PIXI.Graphics)] = mutable.Map()

  def drawAbilityGivers(givers: Map[Entity.Id, AbilityGiver]): Unit = {
    abilityGivers
      .filterNot(elem => givers.isDefinedAt(elem._1))
      .foreach { elem =>
        abilityGiverStage.removeChild(elem._2._1)
        abilityGiverStage.removeChild(elem._2._2)
        abilityGivers -= elem._1
      }

    givers.foreach { case (id, abilityGiver) =>
      val (sprite, mask) = abilityGivers.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val elem = newAbilityGiver(abilityGiver.abilityId)
          abilityGiverStage.addChild(elem._1)
          abilityGiverStage.addChild(elem._2)
          elem._1.anchor.set(0.5, 0.5)

          abilityGivers += (id -> elem)

          elem
      }
      camera.viewportManager(sprite, abilityGiver.pos, abilityGiver.shape.boundingBox)
      camera.viewportManager(mask, abilityGiver.pos, abilityGiver.pos, abilityGiver.shape.boundingBox)
    }
  }

}
