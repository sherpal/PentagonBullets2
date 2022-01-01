package frontend

import com.raquo.laminar.api.L.*
import frontend.AppState.*

object ChooseName {

  def apply(nameRequired: NameRequired, stateChanger: Observer[AnyAppState]) = {
    val name: Var[String] = Var("")

    div(
      h1("Welcome to Pentagon Bullets!"),
      nameRequired.maybeErrorMessage.map(error => p(color := "red", s"Error: $error")),
      form(
        label("Please enter a cool name: "),
        input(
          tpe := "text",
          onMountFocus,
          controlled(
            value <-- name.signal,
            onInput.mapToValue --> name
          )
        ),
        inContext {
          _.events(onSubmit.preventDefault.mapToValue)
            .sample(name.signal)
            .map(Name.PlayerName(_))
            .map(AppState.fromEitherName) --> stateChanger
        }
      )
    )
  }

}
