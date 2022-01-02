package scypress.models

import scypress.Scypress
import facades.Chainable
import facades.JQuery
import org.scalajs.dom.{HTMLElement, HTMLInputElement}

trait Predicate:
  import Predicate._

  def errorMessage: String

  def isSatisfied: Boolean

  final def &&(that: Predicate): Predicate = And(this, that)
  final def ||(that: Predicate): Predicate = Or(this, that)

end Predicate

object Predicate:

  final class PredicateFailedException(message: String) extends RuntimeException(message)

  def fromBoolean(bool: Boolean): Predicate = new Predicate {
    def isSatisfied: Boolean = bool
    def errorMessage: String = s"Condition is not satisfied."
  }

  def fromBoolean(bool: => Boolean, message: => String): Predicate = new Predicate {
    def isSatisfied: Boolean = bool
    def errorMessage: String = message
  }

  def all(predicates: Predicate*): Predicate = predicates.foldLeft(true_)(_ && _)
  def any(predicates: Predicate*): Predicate = predicates.foldLeft(false_)(_ || _)

  val true_ : Predicate  = fromBoolean(true)
  val false_ : Predicate = fromBoolean(false, "Constant false.")

  case class HaveLength(length: Int, elements: List[_]) extends Predicate:
    def isSatisfied: Boolean = elements.length == length
    def errorMessage: String = s"Length of $elements were ${elements.length}, should be $length."

  case class HaveContent(content: String, element: HTMLElement) extends Predicate:
    def isSatisfied: Boolean = content == element.innerHTML
    def errorMessage: String = s"Content was ${element.innerHTML} but I was expecting $content."

  case class HaveValue(value: String, element: HTMLInputElement) extends Predicate:
    def isSatisfied: Boolean = value == element.value
    def errorMessage: String = s"Value was ${element.value} but I was expecting $value."

  case class IsChecked(value: Boolean, element: HTMLInputElement) extends Predicate:
    def isSatisfied: Boolean = value == element.checked
    def errorMessage: String = s"I expected the checked status to be $value."

  final class Or(left: Predicate, right: Predicate) extends Predicate:
    def isSatisfied: Boolean = left.isSatisfied || right.isSatisfied
    def errorMessage: String = s"Both these errors happened: ${left.errorMessage} and ${right.errorMessage}."

  final class And(left: Predicate, right: Predicate) extends Predicate:
    def isSatisfied: Boolean = left.isSatisfied && right.isSatisfied
    def errorMessage: String = if left.isSatisfied then right.errorMessage else left.errorMessage

  object Implicits {

    extension (input: HTMLInputElement)
      def isChecked: Predicate = IsChecked(true, input)

      def isNotChecked: Predicate = IsChecked(false, input)

      def hasValue(value: String): Predicate = HaveValue(value, input)

    extension (elements: List[_]) def haveLength(length: Int): Predicate = HaveLength(length, elements)

  }

end Predicate
