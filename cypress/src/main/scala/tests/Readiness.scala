package tests

import facades.JQuery
import scypress.Scypress
import scypress.Scypress.cy
import scypress.models.Predicate.Implicits.*
import scypress.models.Predicate
import org.scalajs.dom

//noinspection TypeAnnotation
object Readiness {

  val getCheckbox = cy.getJQuery(".ready-checkbox").cast[JQuery[dom.HTMLInputElement]]

  val getReady = for {
    checkbox <- getCheckbox.click()
    if checkbox.isChecked
  } yield ()

}
