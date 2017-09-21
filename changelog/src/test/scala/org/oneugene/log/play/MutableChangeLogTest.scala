package org.oneugene.log.play

import java.time.Month

import org.oneugene.log.model._
import org.scalatest.{FlatSpec, Matchers}

class MutableChangeLogTest extends FlatSpec with Matchers {

  import org.oneugene.log.play.ObjectChangeLens._

  private def loadUser(id: Long): MutableChangeLog[User] = new MutableChangeLog[User] {
    override protected var state: User = User("Ievgenii", BDate(1978, Month.OCTOBER, 3))
  }

  it should "show how mutable changelog works" in {
    val birthDayLens: PropertyChangeLens[User, BDateDay] = UserChangeLogLenses.birthDateLens ^|-> BDateChangeLogLenses.dayLens
    val change = loadUser(1)
      .recordChange(UserChangeLogLenses.nameLens.objectChangeLens.set("Test"))
      .recordChange(birthDayLens.objectChangeLens.set(31))
    val (modifiedUser, changelog) = change.run
    println(s"Changelog test: ${change.run}")

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
