package org.oneugene.log.model

import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository, PropertyChangeReplayImpl}

import scalaz.{Lens, LensFamily, Writer}

case class User(name: String, birthDate: BDate)

object UserChangeLogLenses {

  import scalaz.Scalaz._

  private def setBirthDate(user: User, birthDateLog: Writer[Vector[String], BDate]): Writer[Vector[String], User] =
    birthDateLog.flatMap(birthDate => if (user.birthDate == birthDate) user.set(Vector.empty) else user.copy(birthDate = birthDate).set(Vector("birthDate")))

  private def setName(user: User, nameLog: Writer[Vector[String], String]): Writer[Vector[String], User] =
    nameLog.flatMap(name => if (user.name == name) user.set(Vector.empty) else user.copy(name = name).set(Vector("name")))

  val birthDateLens: PropertyChangeLens[User, BDate] = LensFamily.lensFamilyu(setBirthDate, _.birthDate)

  val nameLens: PropertyChangeLens[User, String] = LensFamily.lensFamilyu(setName, _.name)
}


private[model] object UserLensRepository extends LensRepository[User] {
  private val records = Map[String, LensReplayRecord[User, _]](
    "name" -> LensReplayRecord(Lens.lensu[User, String]((user, name) => user.copy(name = name), _.name), Option.empty),
    "birthDate" -> LensReplayRecord(Lens.lensu[User, BDate]((user, date) => user.copy(birthDate = date), _.birthDate), Some(BDateLensRepository))
  )

  override def resolve[B](propertyName: String): Either[String, LensReplayRecord[User, B]] = {
    val r = records.get(propertyName)
    Either.cond(r.isDefined, r.get.asInstanceOf[LensReplayRecord[User, B]], s"Unknown property $propertyName in class User")
  }
}

object UserPropertyChangeReplay extends PropertyChangeReplayImpl(UserLensRepository)
