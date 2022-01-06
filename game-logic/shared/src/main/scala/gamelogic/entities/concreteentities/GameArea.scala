package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.Complex.i
import gamelogic.gamestate.{GameAction, GameState}
import be.doeraene.physics.shape.{ConvexPolygon, Polygon}
import gamelogic.entities.ActionSource.ServerSource
import gamelogic.entities.{ActionSource, Entity}
import gamelogic.gamestate.gameactions.{NewObstacle, NewPlayer, TranslatePlayer}
import gamelogic.utils.{EntityIdGenerator, IdGeneratorContainer}
import gamelogic.utils.Time
import models.menus.PlayerInfo
import _root_.utils.misc.RGBColour

class GameArea(val width: Int = 1000, val height: Int = 800) {

  def randomPos(): (Double, Double) = (
    (scala.util.Random.nextInt(width - 100) - width / 2 + 50).toDouble,
    (scala.util.Random.nextInt(height - 100) - height / 2 + 50).toDouble
  )

  def randomComplexPos(): Complex = Complex.fromTuple(randomPos())

  def randomComplexPosSatisfying(predicate: Complex => Boolean): Complex =
    LazyList.from(0).map(_ => randomComplexPos()).filter(predicate).head

  /** Returns a random position in the region with specified center and dimensions.
    */
  def randomPos(center: Complex, width: Double, height: Double): (Double, Double) = (
    scala.util.Random.nextInt(width.toInt) + center.re - width / 2,
    scala.util.Random.nextInt(height.toInt) + center.im - height / 2
  )

  def randomPos(mist: Mist): (Double, Double) = {
    val width  = mist.sideLength.toInt
    val height = mist.sideLength.toInt
    (
      (scala.util.Random.nextInt(width - 100) - width / 2 + 50).toDouble,
      (scala.util.Random.nextInt(height - 100) - height / 2 + 50).toDouble
    )
  }

  val topEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(0, height / 2 + 5),
    Obstacle.segmentObstacleVertices(-width / 2 - 10, width / 2 + 10, 10)
  )

  val bottomEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(0, -height / 2 - 5),
    Obstacle.segmentObstacleVertices(-width / 2 - 10, width / 2 + 10, 10)
  )

  val leftEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(-width / 2 - 5, 0),
    Obstacle.segmentObstacleVertices(-i * height / 2, i * height / 2, 10)
  )

  val rightEdgeVertices: (Complex, Vector[Complex]) = (
    Complex(width / 2 + 5, 0),
    Obstacle.segmentObstacleVertices(-i * height / 2, i * height / 2, 10)
  )

  val gameAreaEdgesVertices: List[(Complex, Vector[Complex])] = List(
    topEdgeVertices,
    bottomEdgeVertices,
    leftEdgeVertices,
    rightEdgeVertices
  )

  val gameVertices: Vector[Complex] = Vector(
    topEdgeVertices._1 + leftEdgeVertices._1,
    bottomEdgeVertices._1 + leftEdgeVertices._1,
    bottomEdgeVertices._1 + rightEdgeVertices._1,
    topEdgeVertices._1 + rightEdgeVertices._1
  )

  def gameBounds: Polygon = Polygon(gameVertices)

  def createCenterSquare(radius: Double, source: ActionSource)(using IdGeneratorContainer): NewObstacle = {
    val vertices = (0 to 3).map(j => Complex.rotation(j * math.Pi / 2)).map(_ * radius).toVector

    NewObstacle(GameAction.newId(), Time.currentTime(), Entity.newId(), Complex(0, 0), vertices, source)
  }

  def createGameBoundsBarriers(using IdGeneratorContainer): List[NewObstacle] =
    gameAreaEdgesVertices.map { (obstacleCenter, obstacleVertices) =>
      NewObstacle(
        GameAction.newId(),
        Time.currentTime(),
        Entity.newId(),
        obstacleCenter,
        obstacleVertices,
        ServerSource
      )
    }

  def createPlayer(playerInfo: PlayerInfo, colour: RGBColour)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): NewPlayer = {
    val player = Player(
      Entity.newId(),
      playerInfo.teamId,
      Time.currentTime(),
      playerInfo.name.name,
      allowedAbilities = playerInfo.allowedAbilities,
      relevantUsedAbilities = Map.empty,
      energy = Player.maxEnergy,
      colour = colour
    )
    NewPlayer(GameAction.newId(), player, Time.currentTime(), ServerSource)
  }

  def translateTeams(gameState: GameState, minimalDistances: Double)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): List[TranslatePlayer] = {
    val teams = gameState.players.values.groupBy(_.team).values.map(_.toList)

    teams
      .foldLeft[(GameState, List[TranslatePlayer])]((gameState, List.empty)) {
        case ((gameStateAcc, actionsAcc), nextTeam) =>
          val position =
            randomComplexPosSatisfying(pos =>
              gameStateAcc.players.values.forall(_.pos.distanceTo(pos) > minimalDistances)
            )

          val actions = nextTeam.map(player =>
            TranslatePlayer(GameAction.newId(), Time.currentTime(), player.id, position, ServerSource)
          )

          (gameStateAcc.applyActions(actions), actions ++ actionsAcc)
      }
      ._2

  }

  def createObstacle(gameState: GameState, width: Int, height: Int, source: ActionSource)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): NewObstacle = {
    val pos = randomComplexPos()
    val vertices = Vector(
      Complex(-width / 2, -height / 2),
      Complex(width / 2, -height / 2),
      Complex(width / 2, height / 2),
      Complex(-width / 2, height / 2)
    )

    val obstacleShape: ConvexPolygon = new ConvexPolygon(vertices)

    if (
      gameState.players.values
        .exists(player => player.shape.collides(player.pos, player.rotation, obstacleShape, pos, 0))
    )
      createObstacle(gameState, width, height, source)
    else
      NewObstacle(GameAction.newId(), Time.currentTime(), Entity.newId(), pos, vertices, source)

  }
}

object GameArea {

  def sizeFromNbrPlayers(nbrPlayers: Int): Int = 1000 * (nbrPlayers - 1)

  def apply(nbrPlayers: Int): GameArea = {
    val size = sizeFromNbrPlayers(nbrPlayers)
    new GameArea(size, size)
  }

}
