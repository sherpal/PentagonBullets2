package scypress

import scalajs.js.JSConverters._
import scala.concurrent.Future

import facades.Chainable
import facades.global.{cy => cy0}

import org.scalajs.dom.HTMLElement
import scala.scalajs.js
import org.scalajs.dom
import facades.JQuery

import scypress.models._
import scypress.models.CustomTypes._

private[scypress] object ScypressAST:

  import Scypress.cy

  final class Value[To](to: => To) extends Scypress[To]:
    def run(): Chainable[To] = cy0.wrap(to)

  final class Caster[To, U](from: Scypress[To]) extends Scypress[U]:
    def run(): Chainable[U] = from.run().asInstanceOf[Chainable[U]]

  final class Finder[To](from: Scypress[To], selector: String)(using To <:< HTMLElement) extends Scypress[HTMLElement]:
    def run(): Chainable[HTMLElement] = from.run().find(selector)

  final class FlatMap[To, U](from: Scypress[To], f: To => Scypress[U]) extends Scypress[U]:
    def run(): Chainable[U] = from.run().flatMap(((result: To) => f(result).run()): js.Function1[To, Chainable[U]])

  final class FromFuture[T](future: => Future[T]) extends Scypress[T]:
    import scala.concurrent.ExecutionContext.Implicits.global
    def run(): Chainable[T] = cy0.promiseFlatMap(_ => future.toJSPromise)

  final class GetAlias[T](from: Scypress[Any], alias: Alias[T])(using IsNotNothing[T] =:= true) extends Scypress[T]:
    def run(): Chainable[T] = from.run().get[T]("@" ++ alias.ref)

  final class GetJQuery[To <: HTMLElement](from: Scypress[Any], selector: String) extends Scypress[JQuery[To]]:
    def run(): Chainable[JQuery[To]] =
      from.run()
      cy0.get[JQuery[To]](selector)

  final class MakeAlias[T](from: Scypress[T], alias: Alias[T])(using IsNotUnit[T] =:= true) extends Scypress[T]:
    def run(): Chainable[T] = from.run().as(alias.ref)

  final class Mapped[T, U](from: Scypress[T], f: T => U) extends Scypress[U]:
    def run(): Chainable[U] = from.run().map(f)

  final class Click[To <: HTMLElement](from: Scypress[To]) extends Scypress[To]:
    def run(): Chainable[To] = from.run().click()

  final class Focused(from: Scypress[Any]) extends Scypress[JQuery[HTMLElement]]:
    def run(): Chainable[JQuery[HTMLElement]] =
      from.run()
      cy0.focused()

  final class SomeOrFail[To](from: Scypress[Option[To]]) extends Scypress[To]:
    def run(): Chainable[To] = from
      .run()
      .shouldCallback { maybeTo =>
        if (maybeTo.isEmpty) throw new NoSuchElementException("Option was empty")
      }
      .map(_.get)

  final class Should[To](from: Scypress[To], predicate: To => Predicate) extends Scypress[To]:
    def run(): Chainable[To] = from.run().shouldCallback { (to: To) =>
      val p = predicate(to)
      if !p.isSatisfied then throw Predicate.PredicateFailedException(p.errorMessage)
    }

  final class Typing[To](from: Scypress[To], str: String) extends Scypress[To]:
    def run(): Chainable[To] = from.run().typeContent(str).asInstanceOf[Chainable[JQuery[To]]].map(_.toArray().head)

  final class Visit(url: String) extends Scypress[dom.Window]:
    def run(): Chainable[dom.Window] = cy.run().visit(url)

end ScypressAST
