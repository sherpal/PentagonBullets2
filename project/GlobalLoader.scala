import sbt.Keys.onLoad
import sbt.{Def, _}

object GlobalLoader {

  val asciiPB: String =
    """
      | _____           _                            ____        _ _      _       
      | |  __ \         | |                          |  _ \      | | |    | |      
      | | |__) |__ _ __ | |_ __ _  __ _  ___  _ __   | |_) |_   _| | | ___| |_ ___ 
      | |  ___/ _ \ '_ \| __/ _` |/ _` |/ _ \| '_ \  |  _ <| | | | | |/ _ \ __/ __|
      | | |  |  __/ | | | || (_| | (_| | (_) | | | | | |_) | |_| | | |  __/ |_\__ \
      | |_|   \___|_| |_|\__\__,_|\__, |\___/|_| |_| |____/ \__,_|_|_|\___|\__|___/
      |                            __/ |                                           
      |                           |___/                                                                            
      |""".stripMargin

  /** Display the B12 ascii drawing at launch of sbt. */
  def addOnLoad(): Def.Setting[_] =
    onLoad in Global := {
      println(asciiPB)
      (onLoad in Global).value
    }

}
