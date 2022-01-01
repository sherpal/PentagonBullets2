package tests

import scypress.Tests.{describe, it}
import scypress.Scypress.cy
import scypress.models.Predicate
import scypress.models.TypeSpecialKey.*
import org.scalajs.dom
import facades.JQuery

object Test {
  def main(args: Array[String]): Unit =

    val goToWebsite = cy.visit("http://localhost:8080")

    val shouldBeFocused = for {
      _    <- goToWebsite.unit
      elem <- cy.focused.typeContent("MyCoolName").cast[JQuery[dom.HTMLInputElement]]
      if Predicate.HaveValue("MyCoolName", elem)
      jq <- cy.getJQuery("input")
      if Predicate.HaveLength(1, jq.toArray().toList)
    } yield elem

    val goToGameJoined = for {
      _ <- shouldBeFocused.press(Enter)
    } yield ()

    describe(
      "Testing Stuff",
      it("should have a focused element", shouldBeFocused),
      it("should do stuff", goToGameJoined)
    )
}
