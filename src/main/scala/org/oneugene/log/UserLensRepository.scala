package org.oneugene.log

import org.oneugene.log.UserContainer.{BDate, User}

import scalaz.Lens

object UserLensRepository extends LensRepository[User] {
  private val userLenses = Map[String, Lens[User, _]](
    "name" -> Lens.lensu[User, String]((user, name) => user.copy(name = name), _.name),
    "birthDate" -> Lens.lensu[User, BDate]((user, date) => user.copy(birthDate = date), _.birthDate)
  )

  private val subRepositories = Map[String, LensRepository[_]](
    "name" -> EmptyLensRepository,
    "birthDate" -> BDateLensRepository
  )

  override def getLens[B](propertyName: String): Lens[User, B] = userLenses(propertyName).asInstanceOf[Lens[User, B]]

  override def getSubRepository[B](propertyName: String): LensRepository[B] = subRepositories(propertyName).asInstanceOf[LensRepository[B]]
}
