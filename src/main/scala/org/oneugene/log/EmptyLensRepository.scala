package org.oneugene.log

object EmptyLensRepository extends LensRepository[Nothing]{
  override def getLens[B](propertyName: String) = ???

  override def getSubRepository[B](propertyName: String) = ???
}
