package org.oneugene.log.model

import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository, PropertyChangeReplayImpl}

import scalaz.{Lens, Writer}

case class User(name: String, birthDate: BDate)

/**
  * Contains lenses for User case class.
  * This code could be generated if needed.
  */
object UserChangeLogLenses {

  import monocle.PLens

  import scalaz.Scalaz._

  private val setBirthDate: Writer[Vector[String], BDate] => User => Writer[Vector[String], User] = (birthDateLog) => (user) =>
    birthDateLog.flatMap(birthDate => if (user.birthDate == birthDate) user.set(Vector.empty) else user.copy(birthDate = birthDate).set(Vector("birthDate")))

  private val setName: Writer[Vector[String], String] => User => Writer[Vector[String], User] = (nameLog) => (user) =>
    nameLog.flatMap(name => if (user.name == name) user.set(Vector.empty) else user.copy(name = name).set(Vector("name")))

  val birthDateLens: PropertyChangeLens[User, BDate] = PLens[User, Writer[Vector[String], User], BDate, Writer[Vector[String], BDate]](_.birthDate)(setBirthDate)

  val nameLens: PropertyChangeLens[User, String] = PLens[User, Writer[Vector[String], User], String, Writer[Vector[String], String]](_.name)(setName)
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
