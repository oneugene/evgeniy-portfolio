package org.oneugene.log.replay

trait LensRepository[A] {

  def resolve[B](propertyName: String): Either[String, LensReplayRecord[A, B]]

}
