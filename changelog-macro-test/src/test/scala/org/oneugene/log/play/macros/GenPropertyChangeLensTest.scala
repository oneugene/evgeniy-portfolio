package org.oneugene.log.play.macros

import cats.implicits.catsSyntaxWriterId
import org.oneugene.log.play.PropertyChangeLens
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

/**
  * Tests that [[GenPropertyChangeLensImpl]] generates correct lens
  */
class GenPropertyChangeLensTest extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  case class TestSubject(name: String, value: Int)

  case class SampleWrapper(subObject: TestSubject)

  val nameLens: PropertyChangeLens[TestSubject, String] = GenPropertyChangeLens[TestSubject](_.name)
  val subObjectLens: PropertyChangeLens[SampleWrapper, TestSubject] = GenPropertyChangeLens[SampleWrapper](_.subObject)
  val composedLens: PropertyChangeLens[SampleWrapper, String] = subObjectLens ^|-> nameLens

  "Simple Generated PropertyChangeLens" should "correctly change field value and record changed filed name" in {
    forAll { (original: String, modified: String, value: Int) =>
      whenever(original != modified) {
        val sample = TestSubject(original, value)

        val (history, modifiedSample) = nameLens.set(modified.writer(Vector.empty))(sample).run

        history should contain theSameElementsInOrderAs List("name")
        modifiedSample.name should be(modified)
        modifiedSample.value should be(value)
      }
    }
  }

  "Simple Generated PropertyChangeLens" should "conform \"if I get, then set it back, nothing changes\" law" in {
    forAll { (original: String, value: Int) =>
      val sample = TestSubject(original, value)

      val (history, modifiedSample) = nameLens.set(nameLens.get(sample).writer(Vector.empty))(sample).run

      history should be(empty)
      modifiedSample.name should be(original)
      modifiedSample.value should be(value)
    }
  }

  "Composite Generated PropertyChangeLens" should "property collect changed property path" in {
    forAll { (original: String, modified: String, value: Int) =>
      whenever(original != modified) {
        val sample = SampleWrapper(TestSubject(original, value))

        val (history, modifiedSample) = composedLens.set(modified.writer(Vector.empty))(sample).run

        history should contain theSameElementsInOrderAs List("name", "subObject")
        modifiedSample.subObject.name should be(modified)
      }
    }
  }

  "Composite Generated PropertyChangeLens" should "conform \"if I get, then set it back, nothing changes\" law" in {
    forAll { (original: String, value: Int) =>
      val sample = SampleWrapper(TestSubject(original, value))

      val (history, modifiedSample) = composedLens.set(composedLens.get(sample).writer(Vector.empty))(sample).run

      history should be(empty)
      modifiedSample.subObject.name should be(original)
      modifiedSample.subObject.value should be(value)
    }
  }

}
