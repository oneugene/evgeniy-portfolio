package org.oneugene.log

import java.time.Month

import org.oneugene.log.UserContainer._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.language.postfixOps
import scalaz.Scalaz._
import scalaz._

class UserLensSpec extends FlatSpec with Matchers with
  OptionValues with PropertyChecks {

  val sampleBirthDate = BDate(1978, Month.OCTOBER, 3)
  val sampleUser = User("Ievgenii", sampleBirthDate)
  val userBirthdayHistory: LensFamily[User, Writer[Vector[String], User], BDateDay, Writer[Vector[String], BDateDay]] = BDayLenses.bDayHistory <=< UserLenses.birthDateHistory

  "Birth Date Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val (history, modified) = BDayLenses.bDayHistory.set(sampleBirthDate, BDayLenses.bDayHistory.get(sampleBirthDate).set(Vector())) run

    modified should be(sampleBirthDate)
    history should be(empty)
  }

  "User Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {

    val (history, modified) = UserLenses.birthDateHistory.set(sampleUser, UserLenses.birthDateHistory.get(sampleUser).set(Vector())) run

    modified should be(sampleUser)
    history should be(empty)
  }

  "Composite Lenses" should "property collect changed property path" in {
    val newDay = 4
    val (history, newUser) = userBirthdayHistory.set(sampleUser, newDay.set(Vector())) run

    newUser.birthDate.day should be(newDay)
    history should contain theSameElementsInOrderAs List("day", "birthDate")
  }

  "Composite Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val (history, modified) = userBirthdayHistory.set(sampleUser, userBirthdayHistory.get(sampleUser).set(Vector())) run

    modified should be(sampleUser)
    history should be(empty)
  }
}
