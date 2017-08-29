package org.oneugene.log

import java.time.Month

object UserContainer {

  import scalaz.Scalaz._
  import scalaz._

  type BDateYear = Int
  type BDateDay = Int

  case class BDate(year: BDateYear, month: Month, day: BDateDay)

  case class User(name: String, birthDate: BDate)

  object BDayLenses {
    private def setDay(date: BDate, dayLog: Writer[Vector[String], BDateDay]): Writer[Vector[String], BDate] =
      dayLog.flatMap(day => if (date.day == day) date.set(Vector()) else date.copy(day = day).set(Vector("day")))

    private def getDay(date: BDate): BDateDay = date.day

    //S BDate
    //T Writer[Vector[String], BDate]   BDate=>BDate
    //A BDateDay
    //B Writer[Vector[String], BDateDay] BDateDay=>BDateDay
    val bDayHistory: LensFamily[BDate, Writer[Vector[String], BDate], BDateDay, Writer[Vector[String], BDateDay]] = LensFamily.lensFamilyu(setDay, getDay)
  }

  object UserLenses {

    private def setBirthDate(user: User, birthDateLog: Writer[Vector[String], BDate]): Writer[Vector[String], User] =
      birthDateLog.flatMap(birthDate => if (user.birthDate == birthDate) user.set(Vector()) else user.copy(birthDate = birthDate).set(Vector("birthDate")))

    private def getBirthDate(user: User): BDate = user.birthDate

    val birthDateHistory = LensFamily.lensFamilyu(setBirthDate, getBirthDate)
  }

}
