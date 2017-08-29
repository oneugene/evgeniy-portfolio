package org.oneugene.log

import java.time.Month

import org.oneugene.log.UserContainer.{BDate, BDateDay, BDateYear}

import scalaz.Lens

object BDateLensRepository extends LensRepository[BDate]{
  private val dayLens: Lens[BDate, _] = Lens.lensu[BDate, BDateDay]((date, day) => date.copy(day = day), _.day)
  private val yearLens: Lens[BDate, _] = Lens.lensu[BDate, BDateYear]((date, year) => date.copy(year = year), _.year)
  private val monthLens: Lens[BDate, _] = Lens.lensu[BDate, Month]((date, month) => date.copy(month = month), _.month)
  val bDateLenses: Map[String, Lens[BDate, _]] = Map[String, Lens[BDate, _]](
    "day"-> dayLens,
    "year"->yearLens,
    "month"->monthLens
  )

  override def getLens[B](propertyName: String): Lens[BDate, B] = bDateLenses(propertyName).asInstanceOf[Lens[BDate,B]]

  override def getSubRepository[B](propertyName: String): LensRepository[B] = EmptyLensRepository.asInstanceOf[LensRepository[B]]
}
