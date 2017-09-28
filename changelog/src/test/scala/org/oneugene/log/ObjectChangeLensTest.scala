package org.oneugene.log

import org.oneugene.log.model._
import org.oneugene.log.play.ObjectChangeLens._
import org.oneugene.log.play.{NoChangesRecord, ObjectChangeRecord, ObjectChangelog, PropertyChangeRecord}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers, OptionValues}

class ObjectChangeLensTest extends FlatSpec with Matchers with
  OptionValues with PropertyChecks {

  val sampleTestObject = TestContainerObject(TestSubject(42), "given name")

  "Object Changelog Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val changeLog = ObjectChangelog.empty(sampleTestObject)
    val lens = TestContainerObjectLenses.nameLens.objectChangelogLens
    val modified = lens.set(lens.get(changeLog))(changeLog)

    modified should be(changeLog)
  }

  "Object Change Lenses" should "conform \"if I get, then set it back, nothing changes\" law" in {
    val lens = TestContainerObjectLenses.nameLens.objectChangeLens
    val modified: ObjectChangeRecord[TestContainerObject, String] = lens.set(lens.get(sampleTestObject))(sampleTestObject)

    modified match {
      case PropertyChangeRecord(_, _) =>
        fail(s"Expected NoChangesRecord, but got $modified")
      case NoChangesRecord() =>
    }
  }
}
