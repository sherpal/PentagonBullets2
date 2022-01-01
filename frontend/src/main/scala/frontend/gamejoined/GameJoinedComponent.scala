package frontend.gamejoined

import com.raquo.laminar.api.L.*
import frontend.AppState.*
import gamelogic.abilities.Ability
import models.menus.{AbilityInfo, ClientToServer, GameJoinedInfo, PlayerInfo, PlayerName}
import utils.websocket.JsonWebSocket
import urldsl.language.dummyErrorImpl.*

import scala.scalajs.js.JSON

object GameJoinedComponent {

  urldsl.language.dummyErrorImpl

  def apply(gameJoined: GameJoined, stateChanger: Observer[AnyAppState]) = {
    val socket = JsonWebSocket[GameJoinedInfo, ClientToServer, String](
      root / "game-joined",
      param[String]("player-name"),
      gameJoined.name.value
    )

    val gameInfoEvents: EventStream[GameJoinedInfo] = socket.$in

    val abilityId: Var[Ability.AbilityId] = Var(PlayerInfo.init(PlayerName(gameJoined.name.value)).ability.get)

    val abilitySelector = select(
      AbilityInfo.allAbilityInfo.map { info =>
        option(value := info.id.toString, info.name)
      },
      controlled(
        value <-- abilityId.signal.map(_.toString),
        onChange.mapToValue.map(_.toInt).map(Ability.AbilityId.fromInt(_).get) --> abilityId
      )
    )

    val ready: Var[Boolean] = Var(false)

    val readyCheckbox = input(
      tpe := "checkbox",
      controlled(
        checked <-- ready.signal,
        onClick.mapToChecked --> ready
      )
    )

    div(
      s"Your name will be ${gameJoined.name}",
      onMountCallback(context => socket.open()(using context.owner)),
      socket.$error.map(JSON.stringify(_)).map(NameRequired.failure) --> stateChanger,
      socket.$closed.mapTo(NameRequired.failure("You have been disconnected")) --> stateChanger,
      div(
        readyCheckbox,
        label("Ready?")
      ),
      div(
        label("Chose your ability"),
        abilitySelector,
        p(
          child.text <-- abilityId.signal.map(AbilityInfo.abilityInfoFromId).map(_.description)
        )
      ),
      table(
        thead(
          tr(th("Name"), th("Ready?"))
        ),
        tbody(
          children <-- gameInfoEvents.map(_.players.values.toList).map { allPlayerInfo =>
            allPlayerInfo.map { playerInfo =>
              tr(td(playerInfo.name.name), td(if playerInfo.readyStatus then "Ready" else "Not Ready"))
            }
          }
        )
      )
    )
  }

}
