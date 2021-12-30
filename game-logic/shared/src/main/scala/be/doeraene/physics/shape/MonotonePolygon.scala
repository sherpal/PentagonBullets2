package be.doeraene.physics.shape

import be.doeraene.physics.Complex

final class MonotonePolygon(val vertices: Vector[Complex]) extends Polygon {
  val triangulation: List[Triangle] = Shape.triangulateMonotonePolygon(vertices)
}
