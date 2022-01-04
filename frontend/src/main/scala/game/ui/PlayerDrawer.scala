package game.ui

import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer
import be.doeraene.physics.Complex
import game.Camera
import be.doeraene.physics.shape.BoundingBox
import utils.misc.RGBColour
import typings.pixiJs.mod.{DisplayObject as _, *}
import gamelogic.gamestate.GameState
import gamelogic.entities.*
import gamelogic.entities.concreteentities.Player
import gamelogic.entities.Entity.TeamId

import scala.collection.mutable
import scala.scalajs.js.JSConverters.*

trait PlayerDrawer extends Drawer {

  def playerContainer: ReactiveContainer
  def camera: Camera

  private def playerStage = playerContainer.ref

  private def newPlayer(
      vertices: Seq[Complex],
      plrColor: RGBColour,
      teamColor: RGBColour
  ): Sprite = {

    val innerVertices: Seq[Complex] = vertices.map(z => (z.modulus / 2) * Complex.rotation(z.arg))

    val localCoordsVertices = vertices
      .map(z => (z.re, -z.im))
      .flatMap(elem => Vector(elem._1, elem._2))
      .toJSArray

    val localCoordsInnerVertices = innerVertices
      .map(z => (z.re, -z.im))
      .flatMap(elem => Vector(elem._1, elem._2))
      .toJSArray

    val player = new Sprite(
      application.renderer.generateTexture(
        new Graphics()
          .beginFill(teamColor.intColour)
          .drawPolygon(localCoordsVertices)
          .endFill()
          .beginFill(plrColor.intColour)
          .drawPolygon(localCoordsInnerVertices)
          .endFill(),
        linearScale,
        1
      )
    )

    player

  }

  val players: mutable.Map[Entity.Id, (Sprite, Sprite)] = mutable.Map()

  def drawPlayers(
      state: GameState,
      time: Long,
      teamColors: Map[TeamId, RGBColour]
  ): Unit = {
    val playersAlive = state.players
    players
      .filterNot((id, _) => playersAlive.isDefinedAt(id))
      .foreach { case (id, (bodySprite, pointerSprite)) =>
        playerStage.removeChild(bodySprite)
        playerStage.removeChild(pointerSprite)
        players -= id
      }

    playersAlive.foreach { case (id, player) =>
      val (polygon, circle) = players.get(id) match {
        case Some(elem) =>
          elem
        case None =>
          val (bodySprite, pointerSprite) = (
            newPlayer(
              player.shape.vertices,
              player.colour,
              teamColors(player.team)
            ),
            newDisk(2.0, RGBColour.white)
          )
          val pair = (bodySprite, pointerSprite)
          players += (id -> pair)

          playerStage.addChild(bodySprite)
          playerStage.addChild(pointerSprite)

          bodySprite.anchor.set(0.5, 0.5)
          pointerSprite.anchor.set(0.5, 0.5)

          pair
      }
      val playerPosition = player.pos
      val rot            = (Player.radius - 2) * Complex.rotation(player.rotation)
      camera.viewportManager(polygon, playerPosition, player.shape.boundingBox)
      polygon.rotation = -player.rotation

      val directionDiskPos    = playerPosition + rot
      val directionDiskRadius = math.max(circle.width / 2, 1)
      camera.viewportManager(
        circle,
        directionDiskPos,
        BoundingBox(
          directionDiskRadius,
          directionDiskRadius,
          directionDiskRadius,
          directionDiskRadius
        )
      )
    }

  }

}
