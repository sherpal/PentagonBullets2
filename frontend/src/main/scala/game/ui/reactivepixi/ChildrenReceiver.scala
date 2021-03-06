package game.ui.reactivepixi

import com.raquo.laminar.api.A._
import game.ui.reactivepixi.ReactivePixiElement.ReactiveContainer

object ChildrenReceiver {

  def <--(
      childrenObs: Observable[List[ReactivePixiElement.Base]]
  ): PixiModifier[ReactivePixiElement.ReactiveContainer] =
    new PixiModifier[ReactiveContainer] {

      var currentChildren: Vector[ReactivePixiElement.Base] = Vector.empty

      def apply(element: ReactiveContainer): Unit = {
        element.destroyCallbacks :+= { () =>
          currentChildren.foreach(_.destroy())
        }
        childrenObs.foreach { children =>
          // todo: probably optimize this
          val (remaining, leaving) = currentChildren.partition(children.contains)
          currentChildren = remaining
          leaving.foreach(_.destroy())
          val newChildren = children.filterNot(remaining.contains)
          currentChildren ++= newChildren
          newChildren.foreach(ReactivePixiElement.addChildTo(element, _))
        } {
          element
        }
      }
    }

  def children: ChildrenReceiver.type = this

}
