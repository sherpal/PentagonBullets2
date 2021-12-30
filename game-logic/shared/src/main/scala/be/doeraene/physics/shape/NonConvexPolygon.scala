package be.doeraene.physics.shape

import be.doeraene.physics.Complex

final class NonConvexPolygon(val vertices: Vector[Complex]) extends Polygon {
  lazy val triangulation: List[Triangle] = Shape.earClipping(vertices)
}
