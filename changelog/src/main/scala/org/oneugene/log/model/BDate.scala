package org.oneugene.log.model

import java.time.Month

import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository}

import scalaz.{Lens, Writer}

case class BDate(year: BDateYear, month: Month, day: BDateDay)

/**
  * Contains lenses for User case class.
  * This code could be generated if needed.
  */
object BDateChangeLogLenses {

  import monocle.PLens

  import scalaz.Scalaz._

  private val setDay: Writer[Vector[String], BDateDay] => BDate => Writer[Vector[String], BDate] = (dayChangelog) => (date) =>
    dayChangelog.flatMap(day => if (date.day == day) date.set(Vector.empty) else date.copy(day = day).set(Vector("day")))

  private val setYear: Writer[Vector[String], BDateYear] => BDate => Writer[Vector[String], BDate] = (yearChangelog) => (date) =>
    yearChangelog.flatMap(year => if (date.year == year) date.set(Vector.empty) else date.copy(year = year).set(Vector("year")))

  private val setMonth: Writer[Vector[String], Month] => BDate => Writer[Vector[String], BDate] = (monthChangelog) => (date) =>
    monthChangelog.flatMap(month => if (date.month == month) date.set(Vector.empty) else date.copy(month = month).set(Vector("month")))

  val dayLens: PropertyChangeLens[BDate, BDateDay] = PLens[BDate, Writer[Vector[String], BDate], BDateDay, Writer[Vector[String], BDateDay]](_.day)(setDay)
  val yearLens: PropertyChangeLens[BDate, BDateYear] = PLens[BDate, Writer[Vector[String], BDate], BDateYear, Writer[Vector[String], BDateYear]](_.year)(setYear)
  val monthLens: PropertyChangeLens[BDate, Month] = PLens[BDate, Writer[Vector[String], BDate], Month, Writer[Vector[String], Month]](_.month)(setMonth)
}

private[model] object BDateLensRepository extends LensRepository[BDate] {
  private val dayLens: Lens[BDate, _] = Lens.lensu[BDate, BDateDay]((date, day) => date.copy(day = day), _.day)
  private val yearLens: Lens[BDate, _] = Lens.lensu[BDate, BDateYear]((date, year) => date.copy(year = year), _.year)
  private val monthLens: Lens[BDate, _] = Lens.lensu[BDate, Month]((date, month) => date.copy(month = month), _.month)
  private val bDateLenses: Map[String, Lens[BDate, _]] = Map[String, Lens[BDate, _]](
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
