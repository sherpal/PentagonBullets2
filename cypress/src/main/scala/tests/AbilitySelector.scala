package tests

import facades.JQuery
import org.scalajs.dom.*
import scypress.Scypress
import scypress.Scypress.cy
import gamelogic.abilities.Ability
import models.menus.AbilityInfo
import scypress.models.Predicate

object AbilitySelector {

  val getAbilityDescription: Scypress[JQuery[HTMLElement]] = cy.getJQuery(".ability-description")

  val getAbilitySelector: Scypress[JQuery[HTMLSelectElement]] = cy
    .getJQuery(".ability-selector")
    .cast[JQuery[HTMLSelectElement]]

  def selectAbilityAndVerifyDescription(abilityId: Ability.AbilityId): Scypress[Unit] = for {
    _           <- getAbilitySelector.select(abilityId.toString)
    description <- getAbilityDescription
    if Predicate.HaveContent(AbilityInfo.abilityInfoFromId(abilityId).description, description)
  } yield ()

  val verifyAllAbilities = Scypress.foreach_(Ability.nonShieldAbilityIds)(selectAbilityAndVerifyDescription)

}
