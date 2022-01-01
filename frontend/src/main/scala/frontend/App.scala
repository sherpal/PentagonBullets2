package frontend

import com.raquo.laminar.api.L.*
import frontend.AppState.*

object App {

  def apply() = {
    val appState: Var[AnyAppState] = Var(NameRequired.success)
    val stateObserver              = appState.writer

    div(
      child <-- appState.signal.map {
        case nameRequired: NameRequired => ChooseName(nameRequired, stateObserver)
        case gameJoined: GameJoined     => gamejoined.GameJoinedComponent(gameJoined, stateObserver)
      }
    )
  }
}
