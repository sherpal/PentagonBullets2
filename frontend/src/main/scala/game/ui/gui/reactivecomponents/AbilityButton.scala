package game.ui.gui.reactivecomponents

import com.raquo.laminar.api.A.*
import game.ui.reactivepixi.AttributeModifierBuilder._
import game.ui.reactivepixi.EventModifierBuilder._
import game.ui.reactivepixi.ReactivePixiElement._
import gamelogic.abilities.Ability
import gamelogic.entities.Entity
import gamelogic.gamestate.GameState
import typings.pixiJs.PIXI.Texture
import utils.misc.RGBAColour

final class AbilityButton(
    abilityId: Ability.AbilityId,
    playerId: Entity.Id,
    clickWriter: Observer[Ability.AbilityId],
    texture: Texture,
    overlayTexture: Texture,
    abilityFocusTexture: Texture,
    gameStateUpdates: EventStream[(GameState, Long)],
    dimensions: Signal[(Double, Double)]
) extends GUIComponent {

  val remainingCooldownSignal = gameStateUpdates
    .map { case (gameState, currentTime) =>
      (for {
        player  <- gameState.playerById(playerId)
        lastUse <- player.relevantUsedAbilities.values.filter(_.abilityId == abilityId).maxByOption(_.time)
        elapsedTime = currentTime - lastUse.time
        value       = 1.0 - elapsedTime / (lastUse.cooldown.toDouble / player.abilityCount(abilityId))
      } yield value max 0.0).getOrElse(0.0)

    }
    .toSignal(0.0)

  container.amend(
    pixiSprite(
      texture,
      interactive := true,
      onClick.stopPropagation.mapTo(abilityId) --> clickWriter,
      width  <-- dimensions.map(_._1),
      height <-- dimensions.map(_._2)
    ),
    new StatusBar(
      remainingCooldownSignal,
      Val(RGBAColour(0, 0, 0, 0.4)),
      Val(true),
      overlayTexture,
      dimensions,
      orientation = StatusBar.Vertical
    ): ReactiveContainer,
    pixiSprite(
      abilityFocusTexture,
      width <-- dimensions.map(_._1),
      height <-- dimensions.map(_._2),
      visible <-- remainingCooldownSignal.map(_ == 0.0)
    )
  )

}
