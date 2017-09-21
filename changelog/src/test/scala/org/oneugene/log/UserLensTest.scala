package org.oneugene.log

import java.time.Month

import org.oneugene.log.model.BDateChangeLogLenses._
import org.oneugene.log.model.UserChangeLogLenses._
import org.oneugene.log.model.{BDate, BDateDay, User}
import org.oneugene.log.play.PropertyChangeLens
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.language.postfixOps
import scalaz.Scalaz._

class UserLensTest extends FlatSpec with Matchers with
  OptionValues with PropertyChecks {

  val sampleBirthDate = BDate(1978, Month.OCTOBER, 3)
  val sampleUser = User("Ievgenii", sampleBirthDate)
  val userBirthdayHistory: PropertyChangeLens[User, BDateDay] = birthDateLens ^|-> dayLens

  "Birth Date Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val (history, modified) = dayLens.set(dayLens.get(sampleBirthDate).set(Vector.empty))(sampleBirthDate) run

    modified should be(sampleBirthDate)
    history should be(empty)
  }

  "User Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {

    val (history, modified) = birthDateLens.set(birthDateLens.get(sampleUser).set(Vector.empty))(sampleUser) run

    modified should be(sampleUser)
    history should be(empty)
  }

  "Composite Lenses" should "property collect changed property path" in {
    val newDay = 4
    val (history, newUser) = userBirthdayHistory.set(newDay.set(Vector.empty))(sampleUser) run

    newUser.birthDate.day should be(newDay)
    history should contain theSameElementsInOrderAs List("day", "birthDate")
  }

  "Composite Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val (history, modified) = userBirthdayHistory.set(userBirthdayHistory.get(sampleUser).set(Vector.empty))(sampleUser) run

    modified should be(sampleUser)
    history should be(empty)
  }
}
