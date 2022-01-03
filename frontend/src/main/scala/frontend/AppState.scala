package frontend

import frontend.Name.PlayerName
import models.menus
import models.menus.GameKeys.GameKey
import models.menus.PlayerInfo

sealed trait AppState[Next <: AppState[_, _], Args] {
  def next(args: Args): Next

  final def next(using ev: Unit =:= Args): Next = next(ev(()))
}

object AppState {

  type AnyAppState = AppState[_, _]

  def fromEitherName(maybePlayerName: Either[String, PlayerName]): NameRequired | GameJoined = maybePlayerName match {
    case Left(value)  => NameRequired.failure(value)
    case Right(value) => GameJoined(value)
  }

  case class NameRequired(maybeErrorMessage: Option[String]) extends AppState[GameJoined, PlayerName] {
    def next(name: PlayerName): GameJoined = GameJoined(name)
  }

  object NameRequired {
    def success: NameRequired                  = NameRequired(None)
    def failure(message: String): NameRequired = NameRequired(Some(message))
  }

  case class GameJoined(name: PlayerName)
      extends AppState[NameRequired | GameStarted, Option[String] | (PlayerInfo, GameKey)] {
    def next(args: Option[String] | (PlayerInfo, GameKey)): NameRequired | GameStarted = args match {
      case maybeErrorMessage: Option[String]          => NameRequired(maybeErrorMessage)
      case (playerInfo: PlayerInfo, gameKey: GameKey) => GameStarted(playerInfo, gameKey)
    }
  }

  case class GameStarted(playerInfo: PlayerInfo, gameKey: GameKey) extends AppState[NameRequired, Option[String]] {
    def next(maybeErrorMessage: Option[String]): NameRequired = NameRequired(maybeErrorMessage)

    def playerName: menus.PlayerName = playerInfo.name
  }

}
