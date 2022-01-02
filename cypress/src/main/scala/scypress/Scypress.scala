package scypress

import facades.Chainable
import facades.global.cy as cy0
import org.scalajs.dom.HTMLElement

import scala.scalajs.js
import org.scalajs.dom

import scala.reflect.ClassTag
import scala.concurrent.Future
import scypress.models.*
import scypress.models.CustomTypes.*
import facades.JQuery

import scala.scalajs.js.JSConverters.*

trait Scypress[+To]:

  import ScypressAST._

  def run(): Chainable[To]

  /** Executes this chain first, then that one, garding the result of that.
    */
  def *>[To0](that: Scypress[To0]): Scypress[To0] = flatMap((_: Any) => that)

  /** Executes this chain first, then that one, garding the result of this.
    *
    * Important: the result of this might not be valid anymore after the execution of that! For example, if this value
    * is an HTML element, and that value changes the page, the element might be disappeard, which will cause the effect
    * to fail.
    */
  def <*[To0](that: Scypress[To0]): Scypress[To] = tap(that)

  def alias[To0 >: To](theAlias: Alias[To0])(using IsNotUnit[To0] =:= true): Scypress[To0] = MakeAlias(this, theAlias)

  /** Executes this chain, then ignore the result and replaced it by the argument u.
    *
    * This is *not* the `as` command from cypress. See `alias` for that.
    */
  def as[U](u: => U): Scypress[U] = map((_: Any) => u)

  /** Casts the value contained by this chain into a value of type `U`.
    */
  def cast[U](using ClassTag[U]): Scypress[U] = new Caster[To, U](this)

  /** Clicks on the html element.
    */
  def click()(using ev: To <:< JQuery[HTMLElement]): Scypress[To] = new Click(this)

  def find(selector: String)(using To <:< HTMLElement): Scypress[HTMLElement] =
    new Finder(this, selector)

  /** Executes this chain first, then uses the result to create a new chain, and executes that new chain.
    */
  def flatMap[U](f: To => Scypress[U]): Scypress[U] = FlatMap(this, f)

  def focused: Scypress[JQuery[HTMLElement]] = new Focused(this)

  /** Search for the first html element satisfying the specified CSS selector.
    */
  def get(selector: String): Scypress[HTMLElement] =
    getJQuery(selector).map(_.toArray().head)

  def getAlias[T](alias: Alias[T])(using IsNotNothing[T] =:= true): Scypress[T] =
    GetAlias(this, alias)

  def getJQuery(selector: String): Scypress[JQuery[HTMLElement]] =
    GetJQuery(this, selector)

  /** Search for all the html elements satisfying the specified CSS selector.
    *
    * The returned list is necessarily non empty, otherwise cypress fails.
    */
  def getList(selector: String): Scypress[List[HTMLElement]] =
    getJQuery(selector).map(_.toArray().toList)

  /** Executes this chain, then transforms the result via `f`.
    */
  def map[U](f: To => U): Scypress[U] = flatMap(to => new Value(f(to)))

  def select(value: String)(using ev: To <:< JQuery[dom.HTMLSelectElement]): Scypress[JQuery[dom.HTMLSelectElement]] =
    new SelectOption(ev.liftCo.apply(this), value)

  def someOrFail[To0](using ev: To <:< Option[To0]): Scypress[To0] =
    SomeOrFail(ev.liftCo(this))

  /** Alias for <*
    */
  def tap[A](that: Scypress[A]): Scypress[To] = flatMap((to: To) => that.as(to))

  /** Executes the effect after this chain runs.
    */
  def tapSideEffect[A](effect: => A): Scypress[To] = tap(Value(effect))

  /** Types the content str into the html input.
    */
  def typeContent(str: String)(using ev: To <:< JQuery[HTMLElement]): Scypress[To] = Typing(this, str)

  def press(key: TypeSpecialKey)(using ev: To <:< JQuery[HTMLElement]): Scypress[To] =
    Typing(this, "{" ++ key.value ++ "}")

  /** Executes this chain and ignores the result.
    */
  def unit: Scypress[Unit] = as(())

  def withFilter(predicate: To => Predicate): Scypress[To] = Should(this, predicate)

end Scypress

object Scypress:

  case object cy extends Scypress[Unit]:
    def visit(url: String): Scypress[dom.Window] = ScypressAST.Visit(url)
    def run(): Chainable[Unit]                   = cy0

  def fromFuture[T](future: => Future[T]): Scypress[T] = ScypressAST.FromFuture(future)

  def foreach_[T, U](elements: Iterable[T])(effect: T => Scypress[U]): Scypress[Unit] =
    elements.map(effect).reduceOption(_ *> _).fold(cy)(_.unit)

end Scypress
