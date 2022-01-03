package utils

import com.raquo.laminar.api.L.{onMountCallback, Element, Modifier}
import zio.ZIO

package object laminarzio {

  /** Executes asynchronously the effect when the element is mounted.
    */
  def onMountZIO[El <: Element](zio: ZIO[frontend.GlobalEnv, Nothing, Unit]): Modifier[El] =
    onMountCallback[El](_ => frontend.runtime.unsafeRunToFuture(zio))

}
