package org.oneugene.log

import scalaz.Lens

trait LensRepository[A] {
  def getLens[B](propertyName: String):Lens[A, B]

  def getSubRepository[B](propertyName: String):LensRepository[B]
}
