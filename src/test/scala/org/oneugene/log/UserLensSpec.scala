package org.oneugene.log

import java.time.Month

import org.oneugene.log.UserContainer._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scalaz.Scalaz._
import scalaz._

class UserLensSpec extends FlatSpec with Matchers with
  OptionValues with PropertyChecks {

  val sampleBirthDate = BDate(1978, Month.OCTOBER, 3)
  val sampleUser = User("Ievgenii", sampleBirthDate)
  val userBirthdayHistory: LensFamily[User, Writer[Vector[String], User], BDateDay, Writer[Vector[String], BDateDay]] = BDayLenses.bDayHistory <=< UserLenses.birthDateHistory

  "Composite Lenses" should "property collect changed property path" in {
    val newDay = 4
    val (history, newUser) = userBirthdayHistory.set(sampleUser, newDay.set(Vector())) run

    newUser.birthDate.day should be(newDay)
    history should contain theSameElementsInOrderAs List("day", "birthDate")
  }

  "Id lens" should "be useful" in  {
    val newDay = 4
    val idLens = Lens.lensId[User]

    val (history, newUser) = userBirthdayHistory.set(sampleUser, newDay.set(Vector())) run

    println(idLens.get(sampleUser))
    println(idLens.set(sampleUser, newUser))
  }

  "Composite Lenses" should "be useful" in {
    val newDay = 4
    val originalValue = userBirthdayHistory.get(sampleUser)
    val (history, newUser) = userBirthdayHistory.set(sampleUser, newDay.set(Vector())) run

    val propertyChange = PropertyChange(history, newDay, originalValue)

    val replayedUser = UserPropertyChangeReplay.replayChange(sampleUser, propertyChange)
    println(replayedUser)
    replayedUser.birthDate.day should be(newDay)

    UserPropertyChangeReplay.replayChange(newUser, propertyChange)
  }

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

  "Composite Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {

    val (history, modified) = userBirthdayHistory.set(sampleUser, userBirthdayHistory.get(sampleUser).set(Vector())) run

    modified should be(sampleUser)
    history should be(empty)
  }
}
