package org.oneugene.log.model

import java.time.Month

import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository}

import scalaz.{Lens, LensFamily, Writer}

case class BDate(year: BDateYear, month: Month, day: BDateDay)

/**
  * Contains lenses for User case class.
  * This code could be generated if needed.
  */
object BDateChangeLogLenses {

  import scalaz.Scalaz._

  private def setDay(date: BDate, dayChangelog: Writer[Vector[String], BDateDay]): Writer[Vector[String], BDate] =
    dayChangelog.flatMap(day => if (date.day == day) date.set(Vector.empty) else date.copy(day = day).set(Vector("day")))

  private def setYear(date: BDate, yearChangelog: Writer[Vector[String], BDateYear]): Writer[Vector[String], BDate] =
    yearChangelog.flatMap(year => if (date.year == year) date.set(Vector.empty) else date.copy(year = year).set(Vector("year")))

  private def setMonth(date: BDate, monthChangelog: Writer[Vector[String], Month]): Writer[Vector[String], BDate] =
    monthChangelog.flatMap(month => if (date.month == month) date.set(Vector.empty) else date.copy(month = month).set(Vector("month")))

  val dayLens: PropertyChangeLens[BDate, BDateDay] = LensFamily.lensFamilyu(
    setDay, _.day)

  val yearLens: PropertyChangeLens[BDate, BDateYear] = LensFamily.lensFamilyu(
    setYear, _.year)

  val monthLens: PropertyChangeLens[BDate, Month] = LensFamily.lensFamilyu(
    setMonth, _.month)
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
