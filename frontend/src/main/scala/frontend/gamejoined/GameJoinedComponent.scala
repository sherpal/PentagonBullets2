package frontend.gamejoined

import com.raquo.laminar.api.L._
import frontend.AppState.*

object GameJoinedComponent {

  def apply(gameJoined: GameJoined, stateChanger: Observer[AnyAppState]) = {
    val coucou = 3
    div(s"Your name will be ${gameJoined.name}")
  }

}
