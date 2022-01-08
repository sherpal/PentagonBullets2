package services.localstorage

import zio.ZIO
import models.playing.Controls
import models.playing.KeyboardControls
import models.syntax.Pointed
import org.scalajs.dom
import io.circe.generic.semiauto._
import io.circe.Codec

object controls {

  implicit val controlsCodec: Codec[Controls] = deriveCodec

  private val keyboardControlsKeyM = createKey(KeyboardControls.storageKey)
  private val controlsKeyM         = createKey(Controls.storageKey)

  /** Retrieve the [[Controls]] from the local storage if it exists, or the default one otherwise. */
  val retrieveControls: ZIO[LocalStorage, Nothing, Controls] = for {
    key <- keyboardControlsKeyM
    maybeFromLocalStorage <- retrieveFrom[Controls](key)
      .catchAll(t =>
        ZIO.effectTotal(dom.console.error(s"Failed to retrieve controls: ${t.getMessage}")) *> ZIO
          .some(Pointed[Controls].unit)
      )
    keyboardControls = maybeFromLocalStorage.getOrElse(Pointed[Controls].unit)
  } yield keyboardControls

  /** Stores the given [[Controls]] and returns it. */
  def storeControls(controls: Controls): ZIO[LocalStorage, Throwable, Controls] =
    for {
      key <- keyboardControlsKeyM
      _   <- storeAt(key, controls)
    } yield controls

  val resetControls: ZIO[LocalStorage, Throwable, Unit] = controlsKeyM >>= clearKey

}
