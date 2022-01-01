package scypress

import scala.scalajs.js

object Tests:

  case class it[A](description: String, scypressTest: Scypress[A])

  def describe[A](description: String, testCases: it[A]*): Unit = facades.global.describe(
    description,
    (
        () =>
          testCases.foreach { testCase =>
            facades.global.it(
              testCase.description,
              (() => testCase.scypressTest.run())
            )
          }
    ): js.Function0[Unit]
  )

end Tests
