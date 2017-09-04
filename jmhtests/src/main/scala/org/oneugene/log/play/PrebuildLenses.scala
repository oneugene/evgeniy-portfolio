package org.oneugene.log.play

import java.time.Month

import org.oneugene.log.model._
import org.oneugene.log.play.ObjectChangeLens._

object PrebuildLenses {
  val birthDayLens: PropertyChangeLens[User, BDateDay] = UserChangeLogLenses.birthDateLens >=> BDateChangeLogLenses.dayLens
  val birthMonthLens: PropertyChangeLens[User, Month] = UserChangeLogLenses.birthDateLens >=> BDateChangeLogLenses.monthLens
  val birthYearLens: PropertyChangeLens[User, BDateYear] = UserChangeLogLenses.birthDateLens >=> BDateChangeLogLenses.yearLens

  val nameOcLens: ObjectChangeLens[User, String] = UserChangeLogLenses.nameLens.objectChangeLens
  val birthDayOcLens: ObjectChangeLens[User, BDateDay] = birthDayLens.objectChangeLens
  val birthMonthOcLens: ObjectChangeLens[User, Month] = birthMonthLens.objectChangeLens
  val birthYearOcLens: ObjectChangeLens[User, BDateYear] = birthYearLens.objectChangeLens


  val nameClLens: ObjectChangelogLens[User, String] = UserChangeLogLenses.nameLens.objectChangelogLens
  val birthDayClLens: ObjectChangelogLens[User, BDateDay] = birthDayLens.objectChangelogLens
  val birthMonthClLens: ObjectChangelogLens[User, Month] = birthMonthLens.objectChangelogLens
  val birthYearClLens: ObjectChangelogLens[User, BDateYear] = birthYearLens.objectChangelogLens
}
