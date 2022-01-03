package utils

import com.raquo.laminar.api.L.{onMountCallback, Element, Modifier, MountContext}
import zio.ZIO

package object laminarzio {

  /** Executes asynchronously the effect when the element is mounted.
    */
  def onMountZIO[El <: Element](zio: ZIO[frontend.GlobalEnv, Nothing, Unit]): Modifier[El] =
    onMountZIOWithContext(_ => zio)

  def onMountZIOWithContext[El <: Element](
      effect: MountContext[El] => ZIO[frontend.GlobalEnv, Nothing, Unit]
  ): Modifier[El] =
    onMountCallback[El](ctx => frontend.runtime.unsafeRunToFuture(effect(ctx)))

}
