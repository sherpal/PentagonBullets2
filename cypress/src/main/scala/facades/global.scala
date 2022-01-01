package facades

import scala.scalajs.js

import scala.scalajs.js.annotation._

object global:

  val cy: Chainable[Unit] = js.Dynamic.global.selectDynamic("cy").asInstanceOf[Chainable[Unit]]

  @js.native
  @JSGlobal("it")
  def it(description: String, testCase: js.Function0[Unit]): js.Object = js.native

  @js.native
  @JSGlobal("describe")
  def describe(description: String, suite: js.Function0[Unit]): Unit = js.native

end global
