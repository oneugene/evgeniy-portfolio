package org.oneugene.log.play

import org.oneugene.log.model._
import org.scalatest.{FlatSpec, Matchers}

class MutableChangeLogTest extends FlatSpec with Matchers {

  import org.oneugene.log.play.ObjectChangeLens._

  private def loadUser(): MutableChangeLog[TestContainerObject] = new MutableChangeLog[TestContainerObject] {
    override protected var state: TestContainerObject = TestContainerObject(TestSubject(42), "given name")
  }

  it should "show how mutable changelog works" in {
    val valueLens: PropertyChangeLens[TestContainerObject, Int] = TestContainerObjectLenses.referenceLens ^|-> TestSubjectLenses.valueLens

    val change = loadUser()
      .recordChange(TestContainerObjectLenses.nameLens.objectChangeLens.set("Test"))
      .recordChange(valueLens.objectChangeLens.set(31))
    val (modified, changelog) = change.run

    modified.name should be("Test")
    modified.reference.value should be(31)

    changelog should have size 2

    changelog.head.originalValue should be("given name")
    changelog.head.newValue should be("Test")
    changelog.head.propertyPath should contain theSameElementsInOrderAs List("name")
    changelog.last.originalValue should be(42)
    changelog.last.newValue should be(31)
    changelog.last.propertyPath should contain theSameElementsInOrderAs List("value", "reference")
  }
}
