package org.oneugene.join

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks

class JoinPerformance extends FlatSpec with Matchers with OptionValues with PropertyChecks {
  case class ClassA(id: Int, value: String)
  case class ClassB(id: Int, value: String)

  implicit object AKey extends HashJoinKey[ClassA, Int] {
    override def apply(a: ClassA): Int = a.id
  }
  implicit object BKey extends HashJoinKey[ClassB, Int] {
    override def apply(b: ClassB): Int = b.id
  }
  implicit object ABPredicate extends JoinPredicate[ClassA, ClassB] {
    override def apply(l: ClassA, r: ClassB): Boolean = l.id == r.id
  }
  implicit object ABInnerJoinCombinator extends TupleInnerJoinCombinator[ClassA, ClassB]

  object NestedLoopJoiner extends RegularNestedLoopJoin[ClassA, ClassB]

  object ByLeftHashJoiner extends ByLeftHashJoin[ClassA, ClassB, (ClassA, ClassB), Int]
  object ByRightHashJoiner extends ByRightHashJoin[ClassA, ClassB, (ClassA, ClassB), Int]

  it should "measure performance" in {
    val iters = 2
    val aCount = 10000
    val bCount = 100000

    val as = for (key <- 1 to aCount) yield ClassA(key, s"avalue$key")
    val bs = for (key <- 1 to bCount) yield ClassB(key, s"bvalue$key")
    println("Gen finished")

    for (i <- 1 to iters) {
      val start = System.currentTimeMillis()
      val result = ByLeftHashJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"ByLeftHashJoiner took ${end - start} ms, result size: ${result.size}")
    }

    for (i <- 1 to 0) {
      val start = System.currentTimeMillis()
      val result = ByRightHashJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"ByRightHashJoiner took ${end - start} ms, result size: ${result.size}")
    }

    for (i <- 1 to iters) {
      val start = System.currentTimeMillis()
      val result = NestedLoopJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"NestedLoopJoiner took ${end - start} ms, result size: ${result.size}")
    }
    object ParallelNestedLoopJoiner extends ParallelNestedLoopJoin[ClassA, ClassB]
    for (i <- 1 to iters) {
      val start = System.currentTimeMillis()
      val result = ParallelNestedLoopJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"ParallelNestedLoopJoiner took ${end - start} ms, result size: ${result.size}")
    }
  }

}
