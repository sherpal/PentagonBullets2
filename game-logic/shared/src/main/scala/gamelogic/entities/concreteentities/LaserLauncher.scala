package gamelogic.entities.concreteentities

import be.doeraene.physics.Complex
import be.doeraene.physics.shape.{ConvexPolygon, Polygon}
import gamelogic.entities.{Body, Entity}

final case class LaserLauncher(
    id: Entity.Id,
    pos: Complex,
    ownerId: Entity.Id
) extends Body {

  val rotation: Double = 0.0

  val shape: ConvexPolygon = LaserLauncher.laserLauncherShape

  def time: Long = ???

}

object LaserLauncher {

  val laserLauncherShapeRadius: Int = 20

  //val laserLauncherShape: Circle = new Circle(laserLauncherShapeRadius)
  val laserLauncherShape: ConvexPolygon = Polygon(
    Vector(
      Complex(laserLauncherShapeRadius, laserLauncherShapeRadius),
      Complex(-laserLauncherShapeRadius, laserLauncherShapeRadius),
      Complex(-laserLauncherShapeRadius, -laserLauncherShapeRadius),
      Complex(laserLauncherShapeRadius, -laserLauncherShapeRadius)
    ),
    convex = true
  ).asInstanceOf[ConvexPolygon]

}
