package scala.collection.parallel

object CollectionConverters:

  extension [A](ls: Iterable[A]) def par: Iterable[A] = ls
