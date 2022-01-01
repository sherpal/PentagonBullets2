package facades

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

import org.scalajs.dom

//noinspection ProcedureDeclaration
@js.native
trait Chainable[+T] extends js.Object:

  def as(alias: String): Chainable[T] = js.native

  @JSName("then")
  def map[U](f: js.Function1[T, U]): Chainable[U] = js.native

  @JSName("then")
  def flatMap[U](f: js.Function1[T, Chainable[U]]): Chainable[U] = js.native

  @JSName("then")
  def promiseFlatMap[U](f: js.Function1[T, js.Promise[U]]): Chainable[U] = js.native

  def find[U](selector: String): Chainable[U] = js.native

  def focused(): Chainable[JQuery[dom.HTMLElement]] = js.native

  def get[U](selector: String): Chainable[U] = js.native

  def click(): Chainable[T] = js.native

  def should(chainer: String, value: Any): Chainable[T] = js.native

  @JSName("should")
  def shouldCallback(callback: js.Function1[T, Unit]): Chainable[T] = js.native

  @JSName("type")
  def typeContent(str: String): Chainable[T] = js.native

  def visit(url: String): Chainable[dom.Window] = js.native

  def wrap[A](value: A): Chainable[A] = js.native

end Chainable
