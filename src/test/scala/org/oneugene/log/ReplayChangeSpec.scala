package org.oneugene.log

import java.time.Month

import org.oneugene.log.UserContainer.{BDate, User}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scalaz.{Failure, NonEmptyList, Success, Validation}

class ReplayChangeSpec extends FlatSpec with Matchers with OptionValues {
  val sampleBirthDate = BDate(1978, Month.OCTOBER, 3)
  val sampleUser1 = User("Ievgenii1", sampleBirthDate)
  val sampleUser2 = User("Ievgenii2", sampleBirthDate)

  "Empty path" should "be replayed as root object change" in {
    val changeRecord = PropertyChange(Nil, sampleUser2, sampleUser1)

    val replayed = UserPropertyChangeReplay.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Success(user) => user should equal(sampleUser2)
      case _ => fail(s"Bad validation result received: $replayed")
    }
  }

  "Composite path" should "be replayed correctly" in {
    val newDay = sampleBirthDate.day + 1
    val changeRecord = PropertyChange(Vector("day", "birthDate"), newDay, sampleBirthDate.day)

    val replayed = UserPropertyChangeReplay.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Success(user) => user.birthDate.day should equal(newDay)
      case _ => fail(s"Bad validation result received: $replayed")
    }
  }

  "Replay" should "fail in the case of optimistic lock violation" in {
    val newDay = sampleBirthDate.day + 1
    val changeRecord = PropertyChange(Vector("day", "birthDate"), newDay, sampleBirthDate.day - 1)

    val replayed: Validation[NonEmptyList[String], User] = UserPropertyChangeReplay.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Failure(messages) => messages.list.find(_.contains("Original value mismatch")) shouldBe defined
      case v => s"Replay should end with failure, but got $v"
    }
  }
}
