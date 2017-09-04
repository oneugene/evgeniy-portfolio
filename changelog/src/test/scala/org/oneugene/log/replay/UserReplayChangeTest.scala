package org.oneugene.log.replay

import java.time.Month

import org.oneugene.log.PropertyChange
import org.oneugene.log.model.{BDate, User, UserPropertyChangeReplay}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

class UserReplayChangeTest extends FlatSpec with Matchers with OptionValues {
  val sut: PropertyChangeReplay[User] = UserPropertyChangeReplay
  val sampleBirthDate = BDate(1978, Month.OCTOBER, 3)
  val sampleUser1 = User("Ievgenii1", sampleBirthDate)
  val sampleUser2 = User("Ievgenii2", sampleBirthDate)

  "Empty path" should "be replayed as root object change" in {
    val changeRecord = PropertyChange(Nil, sampleUser2, sampleUser1)

    val replayed = sut.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Right(user) => user should equal(sampleUser2)
      case _ => fail(s"Bad validation result received: $replayed")
    }
  }

  "Composite path" should "be replayed correctly" in {
    val newDay = sampleBirthDate.day + 1
    val changeRecord = PropertyChange(Vector("day", "birthDate"), newDay, sampleBirthDate.day)

    val replayed = sut.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Right(user) => user.birthDate.day should equal(newDay)
      case _ => fail(s"Bad validation result received: $replayed")
    }
  }

  "Replay" should "fail in the case of optimistic lock violation" in {
    val newDay = sampleBirthDate.day + 1
    val changeRecord = PropertyChange(Vector("day", "birthDate"), newDay, sampleBirthDate.day - 1)

    val replayed = sut.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Left(message) => message should include("Original value mismatch")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }

  "Replay" should "fail in the case of bad property type" in {
    val changeRecord = PropertyChange(Vector("name"), 1, 1)

    val replayed = sut.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Left(message) => message should include("Original value mismatch")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }

  "Replay" should "fail in the case of unknown property" in {
    val changeRecord = PropertyChange(Vector("unknown"), 1, 1)

    val replayed = sut.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Left(message) => message should include("Unknown property")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }

  "Replay" should "fail in the case of extra path" in {
    val changeRecord = PropertyChange(Vector("extraProperty", "name"), "Test", "Ievgenii1")

    val replayed = sut.replayChange(sampleUser1, changeRecord)

    replayed match {
      case Left(message) => message should include("Unknown property name")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }
}
