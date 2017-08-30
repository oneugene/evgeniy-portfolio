package org.oneugene.log.replay

import java.time.Month

import org.oneugene.log.UserContainer.{BDate, BDateDay, BDateYear}

import scalaz.Lens

object BDateLensRepository extends LensRepository[BDate] {
  private val dayLens: Lens[BDate, _] = Lens.lensu[BDate, BDateDay]((date, day) => date.copy(day = day), _.day)
  private val yearLens: Lens[BDate, _] = Lens.lensu[BDate, BDateYear]((date, year) => date.copy(year = year), _.year)
  private val monthLens: Lens[BDate, _] = Lens.lensu[BDate, Month]((date, month) => date.copy(month = month), _.month)
  val bDateLenses: Map[String, Lens[BDate, _]] = Map[String, Lens[BDate, _]](
    "day" -> dayLens,
    "year" -> yearLens,
    "month" -> monthLens
  )

  override def resolve[B](propertyName: String): Either[String, LensReplayRecord[BDate, B]] = {
    val opt = bDateLenses.get(propertyName).map(l =>
      LensReplayRecord(l.asInstanceOf[Lens[BDate, B]], Option.empty))
    Either.cond(opt.isDefined, opt.get, s"Unknown property $propertyName in class BDate")
  }
}
