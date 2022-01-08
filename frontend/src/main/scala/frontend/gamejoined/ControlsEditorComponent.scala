package frontend.gamejoined

import com.raquo.laminar.api.L.*
import utils.laminarzio.*
import models.playing.Controls
import models.syntax.Pointed
import services.localstorage.controls._
import zio.ZIO
import models.playing.Controls
import models.playing.Controls.InputCode
import org.scalajs.dom
import typings.std.KeyboardEvent
import utils.domutils.Implicits._

import scala.scalajs.js

object ControlsEditorComponent {

  def apply(): HtmlElement = {
    val controls: Var[Controls] = Var(Pointed[Controls].unit)

    val controlsBus: EventBus[Controls] = new EventBus

    val feedCurrentControl =
      retrieveControls.flatMap(controls => ZIO.effectTotal(controlsBus.writer.onNext(controls)))

    case class AssigningInfo(
        name: String,
        currentCode: InputCode,
        assign: (Controls, InputCode) => Controls,
        currentControls: Controls
    )

    /** Feed here Some an instance of [[AssigningInfo]] to modify a key, and None when the key has been modified. */
    val assigningKeyBus: EventBus[Option[AssigningInfo]] = new EventBus
    val assigningWindowVisible                           = assigningKeyBus.events.map(_.isDefined)

    val waitForAssignWindow = child <-- assigningKeyBus.events.map {
      case None => emptyNode
      case Some(assignInfo) =>
        val assignKeyboardCallback: js.Function1[KeyboardEvent, Unit] = (event: KeyboardEvent) => {
          val effect = storeControls(assignInfo.assign(assignInfo.currentControls, event.toInputCode)) *>
            feedCurrentControl

          frontend.runtime.unsafeRunToFuture(effect)
          event.preventDefault()
          event.stopPropagation()
          assigningKeyBus.writer.onNext(None)
        }

        println("coucou")

        div(
          zIndex := 7,
          position := "fixed",
          top := "0",
          left := "0",
          width := "100%",
          height := "100%",
          onClick.preventDefault.stopPropagation.mapTo(Option.empty[AssigningInfo]) --> assigningKeyBus.writer,
          div(
            s"Press any key to assign ${assignInfo.name}",
            onClick.preventDefault.stopPropagation --> Observer.empty
          ),
          onMountCallback { _ =>
            dom.window.addEventListener("keypress", assignKeyboardCallback)
          },
          onUnmountCallback { _ =>
            dom.window.removeEventListener("keypress", assignKeyboardCallback)
          }
        )
    }

    def controlSetting(name: String, code: InputCode, assign: (Controls, InputCode) => Controls)(implicit
        controls: Controls
    ) =
      div(
        name,
        ": ",
        label(
          code.label,
          onClick.mapTo(Some(AssigningInfo(name, code, assign, controls))) --> assigningKeyBus.writer,
          cursor := "pointer"
        )
      )

    div(
      div(
        className := "col-start-1 col-end-1",
        children <-- controlsBus.events.map { implicit controls =>
          List(
            controlSetting("Up", controls.upKey, (cs, c) => cs.copy(upKey = c)),
            controlSetting("Down", controls.downKey, (cs, c) => cs.copy(downKey = c)),
            controlSetting("Left", controls.leftKey, (cs, c) => cs.copy(leftKey = c)),
            controlSetting("Right", controls.rightKey, (cs, c) => cs.copy(rightKey = c)),
            controlSetting("Shield", controls.shieldAbilityKey, (cs, c) => cs.copy(shieldAbilityKey = c))
          )
        },
        children <-- controlsBus.events.map { implicit controls =>
          controls.abilityKeys.zipWithIndex.map { case (code, idx) =>
            controlSetting(
              s"Ability ${idx + 1}",
              code,
              (cs, c) => cs.copy(abilityKeys = cs.abilityKeys.patch(idx, List(c), 1))
            )
          }
        }
      ),
      onMountZIO(feedCurrentControl),
      waitForAssignWindow
    )
  }

}
