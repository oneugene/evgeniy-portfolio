package org.oneugene.log

import java.time.Month

import org.oneugene.log.UserContainer.{BDate, User}
import org.scalatest.{FlatSpec, Matchers}

class ReplayChangeSpec extends FlatSpec with Matchers {
  val sampleBirthDate = BDate(1978, Month.OCTOBER, 3)
  val sampleUser1 = User("Ievgenii1", sampleBirthDate)
  val sampleUser2 = User("Ievgenii2", sampleBirthDate)

  "Empty path" should "be replayed as root object change" in {
    val changeRecord = PropertyChange(Nil, sampleUser2, sampleUser1)

    val replayedUser = UserPropertyChangeReplay.replayChange(sampleUser1, changeRecord)

    replayedUser should equal(sampleUser2)
  }

  "Composite path" should "be replayed correctly" in {
    val newDay = sampleBirthDate.day + 1
    val changeRecord = PropertyChange(Vector("day", "birthDate"), 4, sampleBirthDate.day)

    val replayedUser = UserPropertyChangeReplay.replayChange(sampleUser1, changeRecord)

    replayedUser.birthDate.day should equal(newDay)
  }
}
