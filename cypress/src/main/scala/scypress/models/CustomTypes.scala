package scypress.models

object CustomTypes:

  type IsNotNothing[A] <: Boolean = A match {
    case Nothing => false
    case _ => true
  }

  type IsNotUnit[A] <: Boolean = A match {
    case Unit => false
    case _ => true
  }

end CustomTypes
