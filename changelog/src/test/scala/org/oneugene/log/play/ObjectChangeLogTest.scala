package org.oneugene.log.play

import java.time.Month

import org.oneugene.log.model._
import org.scalatest.{FlatSpec, Matchers}

class ObjectChangeLogTest extends FlatSpec with Matchers {

  import org.oneugene.log.play.ObjectChangeLens._

  it should "show how to record immutable changes using changelog lens" in {
    val birthDayLens: PropertyChangeLens[User, BDateDay] = UserChangeLogLenses.birthDateLens ^|-> BDateChangeLogLenses.dayLens
    val user = User("Ievgenii", BDate(1978, Month.OCTOBER, 3))

    val modification = birthDayLens.objectChangelogLens.set(31)(
      UserChangeLogLenses.nameLens.objectChangelogLens.set("Test")(ObjectChangelog.empty(user))
    )

    modification match {
      case ObjectChangelog(modifiedUser, changelog) =>
        println(s"Mod user:$modifiedUser, changelog: $changelog")
        modifiedUser.name should be("Test")
        modifiedUser.birthDate.day should be(31)

        changelog should have size 2

        changelog.head.originalValue should be("Ievgenii")
        changelog.head.newValue should be("Test")
        changelog.head.propertyPath should contain theSameElementsInOrderAs List("name")
        changelog.last.originalValue should be(3)
        changelog.last.newValue should be(31)
        changelog.last.propertyPath should contain theSameElementsInOrderAs List("day", "birthDate")
    }
  }

  it should "show how to record immutable changes using State monad" in {
    import cats.data.State
    import State.{get, modify}

    val birthDayLens: PropertyChangeLens[User, BDateDay] = UserChangeLogLenses.birthDateLens ^|-> BDateChangeLogLenses.dayLens
    val user = User("Ievgenii", BDate(1978, Month.OCTOBER, 3))

    val nameChange: (ObjectChangelog[User]) => ObjectChangelog[User] = UserChangeLogLenses.nameLens.objectChangelogLens.set("Test")
    val birthDayChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthDayLens.objectChangelogLens.set(31)
    val program: State[ObjectChangelog[User], Unit] = for {
      _ <- get
      _ <- modify(nameChange)
      _ <- modify(birthDayChange)
    } yield ()

    val modification = program.runS(ObjectChangelog.empty(user)).value

    modification match {
      case ObjectChangelog(modifiedUser, changelog) =>
        println(s"Mod user:$modifiedUser, changelog: $changelog")
        modifiedUser.name should be("Test")
        modifiedUser.birthDate.day should be(31)

        changelog should have size 2

        changelog.head.originalValue should be("Ievgenii")
        changelog.head.newValue should be("Test")
        changelog.head.propertyPath should contain theSameElementsInOrderAs List("name")
        changelog.last.originalValue should be(3)
        changelog.last.newValue should be(31)
        changelog.last.propertyPath should contain theSameElementsInOrderAs List("day", "birthDate")
    }
  }

  it should "show how to record immutable changes using append change method" in {
    val birthDayLens: PropertyChangeLens[User, BDateDay] = UserChangeLogLenses.birthDateLens ^|-> BDateChangeLogLenses.dayLens
    val user = User("Ievgenii", BDate(1978, Month.OCTOBER, 3))

    val modification: ObjectChangelog[User] = ObjectChangelog.empty(user)
      .appendChange(UserChangeLogLenses.nameLens.objectChangeLens.set("Test"))
      .appendChange(birthDayLens.objectChangeLens.set(31))

    modification match {
      case ObjectChangelog(modifiedUser, changelog) =>
        println(s"Mod user:$modifiedUser, changelog: $changelog")
        modifiedUser.name should be("Test")
        modifiedUser.birthDate.day should be(31)

        changelog should have size 2

        changelog.head.originalValue should be("Ievgenii")
        changelog.head.newValue should be("Test")
        changelog.head.propertyPath should contain theSameElementsInOrderAs List("name")
        changelog.last.originalValue should be(3)
        changelog.last.newValue should be(31)
        changelog.last.propertyPath should contain theSameElementsInOrderAs List("day", "birthDate")
    }
  }
}
