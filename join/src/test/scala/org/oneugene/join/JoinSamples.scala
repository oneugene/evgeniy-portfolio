package org.oneugene.join

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks

/**
  * Contains sample code which shows how to use join api
  */
class JoinSamples extends FlatSpec with Matchers with OptionValues with PropertyChecks {

  case class ClassA(id: Int, value: String)

  case class ClassB(id: Int, value: String)

  case class ClassC(id: Int, value: String)

  implicit object AKey extends HashJoinKey[ClassA, Int] {
    override def apply(a: ClassA): Int = a.id
  }

  implicit object BKey extends HashJoinKey[ClassB, Int] {
    override def apply(b: ClassB): Int = b.id
  }

  implicit object ABKey extends HashJoinKey[(ClassA, ClassB), Int] {
    override def apply(ab: (ClassA, ClassB)): Int = ab._1.id
  }

  implicit object CKey extends HashJoinKey[ClassC, Int] {
    override def apply(b: ClassC): Int = b.id
  }

  implicit object ABPredicate extends JoinPredicate[ClassA, ClassB] {
    override def apply(l: ClassA, r: ClassB): Boolean = l.id == r.id
  }

  implicit object ABCPredicate extends JoinPredicate[(ClassA, ClassB), ClassC] {
    override def apply(l: (ClassA, ClassB), r: ClassC): Boolean = l._1.id == r.id
  }

  implicit object ABInnerJoinCombinator extends TupleInnerJoinCombinator[ClassA, ClassB]

  implicit object ABCInnerJoinCombinator extends InnerJoinCombinator[(ClassA, ClassB), ClassC, (ClassA, ClassB, ClassC)] {
    override def apply(l: (ClassA, ClassB), r: ClassC): (ClassA, ClassB, ClassC) = (l._1, l._2, r)
  }

  //  implicit object NestedLoopJoiner extends ParallelNestedLoopJoin
  implicit object NestedLoopJoiner extends RegularNestedLoopJoin

  implicit object ByLeftHashJoiner extends ByLeftHashJoin

  object ByRightHashJoiner extends ByRightHashJoin

  it should "show how to use method with implicits" in {
    val as = Array(ClassA(1, "avalue1"))
    val bs = Array(ClassB(1, "bvalue1"))
    val cs: Traversable[ClassC] = Vector(ClassC(1, "cvalue1"))

    val nestedLoopResult = NestedLoopJoiner.innerJoin(as, bs)
    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val byRightHashResult = ByRightHashJoiner.innerJoin(as, bs)
    println(nestedLoopResult)
    println(byLeftHashResult)
    println(byRightHashResult)

    val res3 = NestedLoopJoiner.innerJoin(nestedLoopResult, cs)
    println(res3)
  }

  it should "show how to use method without implicits" in {
    val as = Array(ClassA(1, "avalue1"))
    val bs = Array(ClassB(1, "bvalue1"))

    val nestedLoopResult = NestedLoopJoiner.innerJoin(as, bs, {(l: ClassA, r: ClassB)=>
      l.id == r.id
    }, {(l: ClassA, r: ClassB)=>
      (l, r)
    })
    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs, {(l: ClassA)=>
      l.id
    }, {(r: ClassB)=>
      r.id
    }, {(l: ClassA, r: ClassB)=>
      (l, r)
    })
    val byRightHashResult = ByRightHashJoiner.innerJoin(as, bs, {(l: ClassA)=>
      l.id
    }, {(r: ClassB)=>
      r.id
    }, {(l: ClassA, r: ClassB)=>
      (l, r)
    })
    println(nestedLoopResult)
    println(byLeftHashResult)
    println(byRightHashResult)
  }

  "NestedLoop Join Type class" should "work" in {
    import NestedLoopJoin._
    val as: Traversable[ClassA] = Vector(ClassA(1, "avalue1"))
    val bs: Traversable[ClassB] = Vector(ClassB(1, "bvalue1"))
    val cs: Traversable[ClassC] = Vector(ClassC(1, "cvalue1"))

    val res1 = (as, bs).innerJoin
    val res13 = (res1, cs).innerJoin

    println("Nested loop type class")
    println(res1)
    println(res13)

    val res2 = as innerJoin bs innerJoin cs
    println(res2)

  }

  "Hash Join Type class" should "work" in {
    import HashJoin._
    val as: Traversable[ClassA] = Vector(ClassA(1, "avalue1"))
    val bs: Traversable[ClassB] = Vector(ClassB(1, "bvalue1"))

    val res = (as, bs).innerJoin
    val res2 = as innerJoin bs

    println("Hash type class")
    println(res)
    println(res2)
  }
}
