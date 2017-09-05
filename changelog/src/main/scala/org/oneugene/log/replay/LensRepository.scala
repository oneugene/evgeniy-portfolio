package org.oneugene.log.replay

/**
  * Service description which can return lenses focused to the propertyName for given object type
  * and provided sub repository in the case if property type is product with properties itself.
  *
  * @tparam A type to return lenses for
  */
trait LensRepository[A] {
  /**
    *
    * @param propertyName name of the property to find lens for
    * @tparam B type of the property
    * @return lenses focused to the propertyName for given object type
    *         and provided sub repository in the case if property type is product with properties itself
    *         or string with error message in the case of unknown property
    */
  def resolve[B](propertyName: String): Either[String, LensReplayRecord[A, B]]
}
