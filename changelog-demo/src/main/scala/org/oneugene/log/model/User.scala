package org.oneugene.log.model

import monocle.macros.GenLens
import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.play.macros.GenPropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository, PropertyChangeReplayImpl}

case class User(name: String, birthDate: BDate)

/**
  * Contains lenses for User case class.
  */
object UserChangeLogLenses {
  val birthDateLens: PropertyChangeLens[User, BDate] = GenPropertyChangeLens[User](_.birthDate)

  val nameLens: PropertyChangeLens[User, String] = GenPropertyChangeLens[User](_.name)
}


private[model] object UserLensRepository extends LensRepository[User] {
  private val records = Map[String, LensReplayRecord[User, _]](
    "name" -> LensReplayRecord(GenLens[User](_.name), Option.empty),
    "birthDate" -> LensReplayRecord(GenLens[User](_.birthDate), Some(BDateLensRepository))
  )

  override def resolve[B](propertyName: String): Either[String, LensReplayRecord[User, B]] = {
    val r = records.get(propertyName)
    Either.cond(r.isDefined, r.get.asInstanceOf[LensReplayRecord[User, B]], s"Unknown property $propertyName in class User")
  }
}

object UserPropertyChangeReplay extends PropertyChangeReplayImpl(UserLensRepository)
