package frontend

object Name {
  opaque type PlayerName = String

  object PlayerName {
    extension (name: PlayerName) def value: String = name: String

    val forbiddenCharacters: List[Char] = List(' ', '?', '!', '#', '<', '>')

    def apply(name: String): Either[String, PlayerName] = {
      val errorMessage =
        Option
          .when(name.isEmpty)("Name can't be empty")
          .orElse(
            forbiddenCharacters
              .find(name.contains)
              .map(char =>
                s"Name can't contain '$char' (nor any of the following: ${forbiddenCharacters.filterNot(_ == char).mkString(", ")})."
              )
          )

      errorMessage.toLeft(name)
    }
  }

}
