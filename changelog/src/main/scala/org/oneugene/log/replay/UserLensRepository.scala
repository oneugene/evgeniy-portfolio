package org.oneugene.log.replay

import org.oneugene.log.UserContainer.{BDate, User}

import scalaz.Lens

object UserLensRepository extends LensRepository[User] {
  private val records = Map[String, LensReplayRecord[User, _]](
    "name" -> LensReplayRecord(Lens.lensu[User, String]((user, name) => user.copy(name = name), _.name), Option.empty),
    "birthDate" -> LensReplayRecord(Lens.lensu[User, BDate]((user, date) => user.copy(birthDate = date), _.birthDate), Some(BDateLensRepository))
  )

  override def resolve[B](propertyName: String): Either[String, LensReplayRecord[User, B]] = {
    val r = records.get(propertyName)
    Either.cond(r.isDefined, r.get.asInstanceOf[LensReplayRecord[User, B]], s"Unknown property $propertyName in class User")
  }
}
