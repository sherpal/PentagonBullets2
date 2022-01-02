package frontend.gamejoined

import com.raquo.laminar.api.L.*
import frontend.AppState.*
import gamelogic.abilities.Ability
import gamelogic.entities.Entity.TeamId
import models.menus.{AbilityInfo, ClientToServer, GameJoinedInfo, PlayerInfo, PlayerName}
import utils.websocket.JsonWebSocket
import urldsl.language.dummyErrorImpl.*

import scala.scalajs.js.JSON

object GameJoinedComponent {

  def apply(gameJoined: GameJoined, stateChanger: Observer[AnyAppState]) = {

    def playerName: PlayerName = PlayerName(gameJoined.name.value)

    val socket: JsonWebSocket[GameJoinedInfo, ClientToServer] =
      JsonWebSocket[GameJoinedInfo, ClientToServer, String](
        root / "game-joined",
        param[String]("player-name"),
        playerName.name
      )

    val gameInfoEvents: EventStream[GameJoinedInfo] = socket.$in

    val abilityId: Var[Ability.AbilityId] = Var(PlayerInfo.init(PlayerName(gameJoined.name.value), 0).ability)

    val abilitySelector = select(
      AbilityInfo.allAbilityInfo.map { info =>
        option(value := info.id.toString, info.name)
      },
      controlled(
        value <-- abilityId.signal.map(_.toString),
        onChange.mapToValue.map(_.toInt).map(Ability.AbilityId.fromInt(_).get) --> abilityId
      ),
      abilityId.signal.changes.map(ClientToServer.SelectAbilityId.apply) --> socket
    )

    val teamIdSelector = select(
      children <-- gameInfoEvents
        .map(_.allTeamIdsWithUnused)
        .map(_.map { teamId =>
          option(value := teamId.toString, s"Team $teamId")
        }),
      controlled(
        value <-- gameInfoEvents.map(_.players.get(playerName)).collect { case Some(player) => player.teamId.toString },
        onChange.mapToValue.map(_.toInt).map(ClientToServer.ChangeTeamId.apply) --> socket
      )
    )

    val ready: Var[Boolean] = Var(false)

    val readyCheckbox = input(
      tpe := "checkbox",
      controlled(
        checked <-- ready.signal,
        onClick.mapToChecked --> ready
      ),
      ready.signal.changes.map(ClientToServer.ChangeReadyStatus.apply) --> socket
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
      div(
        label("Chose your team"),
        teamIdSelector
      ),
      table(
        thead(
          tr(th("Name"), th("Team"), th("Ready?"))
        ),
        tbody(
          children <-- gameInfoEvents.map(_.players.values.toList).map { allPlayerInfo =>
            allPlayerInfo.map { playerInfo =>
              tr(
                td(playerInfo.name.name),
                td(playerInfo.teamId.toString),
                td(if playerInfo.readyStatus then "Ready" else "Not Ready")
              )
            }
          }
        )
      )
    )
  }

}
