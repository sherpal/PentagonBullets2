package facades

import scala.annotation.unchecked.uncheckedVariance
import scala.scalajs.js
import scala.scalajs.js.annotation.JSBracketAccess
import org.scalajs.dom

@js.native
trait JQuery[+El] extends js.Object {
  def toArray(): js.Array[El @uncheckedVariance] = js.native

  @JSBracketAccess
  def apply(index: Int): El = js.native
}

object JQuery {

  given conversion[T <: dom.HTMLElement]: Conversion[JQuery[T], T] with
    def apply(jq: JQuery[T]): T = jq(0)

}
