package org.oneugene.log.replay

import org.oneugene.log.PropertyChange
import org.oneugene.log.model.{TestContainerObject, TestPropertyChangeReplay, TestSubject}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

class ReplayChangeTest extends FlatSpec with Matchers with OptionValues {
  val sut: PropertyChangeReplay[TestContainerObject] = TestPropertyChangeReplay
  val sampleSubject = TestSubject(42)
  val sample1 = TestContainerObject(sampleSubject, "Name 1")
  val sample2 = TestContainerObject(sampleSubject, "Name 2")

  "Empty path" should "be replayed as root object change" in {
    val changeRecord = PropertyChange(Nil, sample2, sample1)

    val replayed = sut.replayChange(sample1, changeRecord)

    replayed match {
      case Right(modified) => modified should equal(sample2)
      case _ => fail(s"Bad validation result received: $replayed")
    }
  }

  "Composite path" should "be replayed correctly" in {
    val newValue = sampleSubject.value + 1
    val changeRecord = PropertyChange(Vector("value", "reference"), newValue, sampleSubject.value)

    val replayed = sut.replayChange(sample1, changeRecord)

    replayed match {
      case Right(modified) => modified.reference.value should equal(newValue)
      case _ => fail(s"Bad validation result received: $replayed")
    }
  }

  "Replay" should "fail in the case of optimistic lock violation" in {
    val newValue = sampleSubject.value + 1
    val changeRecord = PropertyChange(Vector("value", "reference"), newValue, sampleSubject.value - 1)

    val replayed = sut.replayChange(sample1, changeRecord)

    replayed match {
      case Left(message) => message should include("Original value mismatch")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }

  "Replay" should "fail in the case of bad property type" in {
    val changeRecord = PropertyChange(Vector("name"), 1, 1)

    val replayed = sut.replayChange(sample1, changeRecord)

    replayed match {
      case Left(message) => message should include("Original value mismatch")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }

  "Replay" should "fail in the case of unknown property" in {
    val changeRecord = PropertyChange(Vector("unknown"), 1, 1)

    val replayed = sut.replayChange(sample1, changeRecord)

    replayed match {
      case Left(message) => message should include("Unknown property")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }

  "Replay" should "fail in the case of extra path" in {
    val changeRecord = PropertyChange(Vector("extraProperty", "name"), "Test1", "Test2")

    val replayed = sut.replayChange(sample1, changeRecord)

    replayed match {
      case Left(message) => message should include("Unknown property name")
      case v => fail(s"Replay should end with failure, but got $v")
    }
  }
}
