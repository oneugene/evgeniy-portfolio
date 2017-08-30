package org.oneugene.join

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks

class JoinTest extends FlatSpec with Matchers with OptionValues with PropertyChecks {

  case class A(key: String, avalue: String)

  case class B(key: Int, bvalue: String)

  implicit object AKey extends HashJoinKey[A, Int] {
    override def apply(a: A): Int = a.key.toInt
  }

  implicit object BKey extends HashJoinKey[B, Int] {
    override def apply(b: B): Int = b.key
  }

  implicit object ABEqObj extends JoinPredicate[A, B] {
    override def apply(l: A, r: B): Boolean = l.key.toInt == r.key
  }

  implicit object ABInnerJoinCombinator extends TupleInnerJoinCombinator[A, B]

  implicit object ABLeftOuterJoinCombinator extends TupleLeftOuterJoinCombinator[A, B]

  object NestedLoopJoiner extends RegularNestedLoopJoin

  object ByLeftHashJoiner extends ByLeftHashJoin

  object ByRightHashJoiner extends ByRightHashJoin

  object ParallelNestedLoopJoiner extends ParallelNestedLoopJoin

  "Join result for collections with same ids " should "return all collections elemens" in {
    val as = List(A("1", "avalue1"), A("2", "avalue2.1"), A("2", "avalue2.2"), A("3", "avalue3"))
    val bs = List(B(1, "bvalue1"), B(2, "bvalue2.1"), B(2, "bvalue2.2"), B(3, "avalue3"))

    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val byRightHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val nestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)
    val parallelNestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)

    val expectedResult = List(
      A("1", "avalue1") -> B(1, "bvalue1"),
      A("2", "avalue2.1") -> B(2, "bvalue2.1"),
      A("2", "avalue2.1") -> B(2, "bvalue2.2"),
      A("2", "avalue2.2") -> B(2, "bvalue2.1"),
      A("2", "avalue2.2") -> B(2, "bvalue2.2"),
      A("3", "avalue3") -> B(3, "avalue3"))

    byLeftHashResult should contain theSameElementsAs expectedResult
    byRightHashResult should contain theSameElementsAs expectedResult
    nestedLoopResult should contain theSameElementsAs expectedResult
    parallelNestedLoopResult should contain theSameElementsAs expectedResult
  }

  "Extra record in left collection" should "be skipped from inner join result" in {
    val as = List(A("1", "avalue1"), A("2", "avalue2"))
    val bs = List(B(1, "bvalue1"))

    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val byRightHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val nestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)
    val parallelNestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)

    val expectedResult = List(
      A("1", "avalue1") -> B(1, "bvalue1"))

    byLeftHashResult should contain theSameElementsAs expectedResult
    byRightHashResult should contain theSameElementsAs expectedResult
    nestedLoopResult should contain theSameElementsAs expectedResult
    parallelNestedLoopResult should contain theSameElementsAs expectedResult
  }

  "Extra record in left collection" should "be in left outer join result with empty right part" in {
    val as = List(A("1", "avalue1"), A("2", "avalue2"))
    val bs = List(B(1, "bvalue1"))

    val nestedLoopResult = NestedLoopJoiner.leftOuterJoin(as, bs)
    val parallelNestedLoopResult = NestedLoopJoiner.leftOuterJoin(as, bs)

    val expectedResult = List(
      A("1", "avalue1") -> Some(B(1, "bvalue1")),
      A("2", "avalue2") -> None
    )

    nestedLoopResult should contain theSameElementsAs expectedResult
    parallelNestedLoopResult should contain theSameElementsAs expectedResult
  }

  "Extra record in right collection" should "be skipped from inner join result" in {
    val as = List(A("1", "avalue1"))
    val bs = List(B(1, "bvalue1"), B(2, "bvalue2"))

    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val byRightHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val nestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)
    val parallelNestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)

    val expectedResult = List(
      A("1", "avalue1") -> B(1, "bvalue1"))

    byLeftHashResult should contain theSameElementsAs expectedResult
    byRightHashResult should contain theSameElementsAs expectedResult
    nestedLoopResult should contain theSameElementsAs expectedResult
    parallelNestedLoopResult should contain theSameElementsAs expectedResult
  }

  "Extra record in right collection" should "be skipped from left outer join result" in {
    val as = List(A("1", "avalue1"))
    val bs = List(B(1, "bvalue1"), B(2, "bvalue2"))

    val nestedLoopResult = NestedLoopJoiner.leftOuterJoin(as, bs)
    val parallelNestedLoopResult = NestedLoopJoiner.leftOuterJoin(as, bs)

    val expectedResult = List(
      A("1", "avalue1") -> Some(B(1, "bvalue1")))

    nestedLoopResult should contain theSameElementsAs expectedResult
    parallelNestedLoopResult should contain theSameElementsAs expectedResult
  }

  "Inner join for empty lists" should "produce empty result" in {
    val as: Traversable[A] = Nil
    val bs: Traversable[B] = Nil

    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val byRightHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val nestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)
    val parallelNestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)

    byLeftHashResult shouldBe empty
    byRightHashResult shouldBe empty
    nestedLoopResult shouldBe empty
    parallelNestedLoopResult shouldBe empty
  }

  "Collections with no common records" should "produce empty inner join result" in {
    val as = List(A("1", "avalue1"))
    val bs = List(B(2, "bvalue2"))

    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val byRightHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val nestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)
    val parallelNestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)

    byLeftHashResult shouldBe empty
    byRightHashResult shouldBe empty
    nestedLoopResult shouldBe empty
    parallelNestedLoopResult shouldBe empty
  }

}
