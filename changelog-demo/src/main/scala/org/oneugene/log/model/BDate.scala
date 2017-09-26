package org.oneugene.log.model

import java.time.Month

import monocle.Lens
import monocle.macros.GenLens
import org.oneugene.log.play.PropertyChangeLens
import org.oneugene.log.play.macros.GenPropertyChangeLens
import org.oneugene.log.replay.{LensReplayRecord, LensRepository}

case class BDate(year: BDateYear, month: Month, day: BDateDay)

/**
  * Contains lenses for User case class.
  */
object BDateChangeLogLenses {
  val dayLens: PropertyChangeLens[BDate, BDateDay] = GenPropertyChangeLens[BDate](_.day)
  val yearLens: PropertyChangeLens[BDate, BDateYear] = GenPropertyChangeLens[BDate](_.year)
  val monthLens: PropertyChangeLens[BDate, Month] = GenPropertyChangeLens[BDate](_.month)
}

private[model] object BDateLensRepository extends LensRepository[BDate] {
  private val dayLens: Lens[BDate, _] = GenLens[BDate](_.day)
  private val yearLens: Lens[BDate, _] = GenLens[BDate](_.year)
  private val monthLens: Lens[BDate, _] = GenLens[BDate](_.month)
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
