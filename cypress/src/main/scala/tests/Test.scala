package tests

import scypress.Tests.{describe, it}
import scypress.Scypress.cy
import scypress.models.Predicate.Implicits.*
import scypress.models.TypeSpecialKey.*
import org.scalajs.dom
import facades.JQuery

object Test {
  def main(args: Array[String]): Unit =

    val goToWebsite = cy.visit("http://localhost:8080").unit

    val shouldBeFocused = for {
      _    <- goToWebsite
      elem <- cy.focused.typeContent("MyCoolName").cast[JQuery[dom.HTMLInputElement]]
      if elem hasValue "MyCoolName"
      jq <- cy.getJQuery("input")
      if jq.toArray().toList haveLength 1
    } yield elem

    val goToGameJoined = shouldBeFocused.press(Enter)

    val theTest = for {
      _ <- goToGameJoined
      _ <- AbilitySelector.verifyAllAbilities
    } yield ()

    val connectAndReady = for {
      _ <- goToGameJoined
      _ <- Readiness.getReady
    } yield ()

    describe(
      "Testing Stuff",
      it("should have a focused element", shouldBeFocused),
      it("should have all the correct descriptions for the abilities", theTest),
      it("should get ready", connectAndReady)
    )
}
