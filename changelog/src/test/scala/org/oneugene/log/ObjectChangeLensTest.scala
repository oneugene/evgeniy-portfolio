package org.oneugene.log

import java.time.Month

import org.oneugene.log.model.UserChangeLogLenses._
import org.oneugene.log.model.{BDate, User}
import org.oneugene.log.play.ObjectChangeLens._
import org.oneugene.log.play.{NoChangesRecord, ObjectChangeRecord, ObjectChangelog, PropertyChangeRecord}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}

import scala.language.postfixOps

class ObjectChangeLensTest extends FlatSpec with Matchers with
  OptionValues with PropertyChecks {

  val sampleBirthDate = BDate(1978, Month.OCTOBER, 3)
  val sampleUser = User("Ievgenii", sampleBirthDate)

  "Object Changelog Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val changeLog = ObjectChangelog.empty(sampleUser)
    val lens = nameLens.objectChangelogLens
    val modified = lens.set(changeLog, lens.get(changeLog))

    modified should be(changeLog)
  }

  "Object Change Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val lens = nameLens.objectChangeLens
    val modified: ObjectChangeRecord[User, String] = lens.set(sampleUser, lens.get(sampleUser))

    modified match {
      case PropertyChangeRecord(_, _) =>
        fail(s"Expected NoChangesRecord, but got $modified")
      case NoChangesRecord() =>
    }
  }
}
