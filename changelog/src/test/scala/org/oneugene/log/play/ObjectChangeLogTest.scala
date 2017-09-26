package org.oneugene.log.play

import org.oneugene.log.model.{TestContainerObject, _}
import org.scalatest.{FlatSpec, Matchers}

class ObjectChangeLogTest extends FlatSpec with Matchers {

  import org.oneugene.log.play.ObjectChangeLens._

  it should "show how to record immutable changes using changelog lens" in {
    val valueLens: PropertyChangeLens[TestContainerObject, Int] = TestContainerObjectLenses.referenceLens ^|-> TestSubjectLenses.valueLens
    val originalObject = TestContainerObject(TestSubject(42), "given name")

    val modification = valueLens.objectChangelogLens.set(31)(
      TestContainerObjectLenses.nameLens.objectChangelogLens.set("Test")(ObjectChangelog.empty(originalObject))
    )

    modification match {
      case ObjectChangelog(modifiedObject, changelog) =>
        modifiedObject.name should be("Test")
        modifiedObject.reference.value should be(31)

        changelog should have size 2

        changelog.head.originalValue should be("given name")
        changelog.head.newValue should be("Test")
        changelog.head.propertyPath should contain theSameElementsInOrderAs List("name")
        changelog.last.originalValue should be(42)
        changelog.last.newValue should be(31)
        changelog.last.propertyPath should contain theSameElementsInOrderAs List("value", "reference")
    }
  }

  it should "show how to record immutable changes using State monad" in {
    import cats.data.State
    import State.{get, modify}

    val valueLens: PropertyChangeLens[TestContainerObject, Int] = TestContainerObjectLenses.referenceLens ^|-> TestSubjectLenses.valueLens
    val originalObject = TestContainerObject(TestSubject(42), "given name")

    val nameChange: (ObjectChangelog[TestContainerObject]) => ObjectChangelog[TestContainerObject] = TestContainerObjectLenses.nameLens.objectChangelogLens.set("Test")
    val valueChange: (ObjectChangelog[TestContainerObject]) => ObjectChangelog[TestContainerObject] = valueLens.objectChangelogLens.set(31)
    val program: State[ObjectChangelog[TestContainerObject], Unit] = for {
      _ <- get
      _ <- modify(nameChange)
      _ <- modify(valueChange)
    } yield ()

    val modification = program.runS(ObjectChangelog.empty(originalObject)).value

    modification match {
      case ObjectChangelog(modifiedObject, changelog) =>
        modifiedObject.name should be("Test")
        modifiedObject.reference.value should be(31)

        changelog should have size 2

        changelog.head.originalValue should be("given name")
        changelog.head.newValue should be("Test")
        changelog.head.propertyPath should contain theSameElementsInOrderAs List("name")
        changelog.last.originalValue should be(42)
        changelog.last.newValue should be(31)
        changelog.last.propertyPath should contain theSameElementsInOrderAs List("value", "reference")
    }
  }

  it should "show how to record immutable changes using append change method" in {
    val valueLens: PropertyChangeLens[TestContainerObject, Int] = TestContainerObjectLenses.referenceLens ^|-> TestSubjectLenses.valueLens
    val originalObject = TestContainerObject(TestSubject(42), "given name")

    val modification: ObjectChangelog[TestContainerObject] = ObjectChangelog.empty(originalObject)
      .appendChange(TestContainerObjectLenses.nameLens.objectChangeLens.set("Test"))
      .appendChange(valueLens.objectChangeLens.set(31))

    modification match {
      case ObjectChangelog(modifiedObject, changelog) =>
        modifiedObject.name should be("Test")
        modifiedObject.reference.value should be(31)

        changelog should have size 2

        changelog.head.originalValue should be("given name")
        changelog.head.newValue should be("Test")
        changelog.head.propertyPath should contain theSameElementsInOrderAs List("name")
        changelog.last.originalValue should be(42)
        changelog.last.newValue should be(31)
        changelog.last.propertyPath should contain theSameElementsInOrderAs List("value", "reference")
    }
  }
}
