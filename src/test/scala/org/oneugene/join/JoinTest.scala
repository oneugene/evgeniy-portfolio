package org.oneugene.join

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import org.scalatest.prop.PropertyChecks

class JoinTest extends FlatSpec with Matchers with OptionValues with PropertyChecks {

  case class ClassA(id:Int, value:String)
  case class ClassB(id:Int, value:String)
  case class ClassC(id:Int, value:String)

  implicit object AKey extends HashJoinKey[ClassA, Int]{
    override def apply(a: ClassA): Int = a.id
  }
  implicit object BKey extends HashJoinKey[ClassB, Int]{
    override def apply(b: ClassB): Int = b.id
  }
  implicit object ABKey extends HashJoinKey[(ClassA, ClassB), Int]{
    override def apply(ab:(ClassA, ClassB)): Int = ab._1.id
  }
  implicit object CKey extends HashJoinKey[ClassC, Int]{
    override def apply(b: ClassC): Int = b.id
  }
  implicit object ABPredicate extends JoinPredicate[ClassA, ClassB]{
    override def apply(l: ClassA, r: ClassB): Boolean = l.id==r.id
  }
  implicit object ABCPredicate extends JoinPredicate[(ClassA, ClassB), ClassC]{
    override def apply(l: (ClassA, ClassB), r: ClassC): Boolean = l._1.id==r.id
  }
  implicit object ABInnerJoinCombinator extends TupleInnerJoinCombinator[ClassA, ClassB]
  implicit object ABCInnerJoinCombinator extends InnerJoinCombinator[(ClassA, ClassB), ClassC, (ClassA, ClassB, ClassC)]{
    override def apply(l: (ClassA, ClassB), r: ClassC): (ClassA, ClassB, ClassC) = (l._1, l._2, r)
  }
  
//  implicit object NestedLoopJoiner extends ParallelNestedLoopJoin[ClassA, ClassB, (ClassA, ClassB)]
  implicit object NestedLoopJoiner extends RegularNestedLoopJoin[ClassA, ClassB, (ClassA, ClassB)]

  implicit object ByLeftHashJoiner extends ByLeftHashJoin[ClassA, ClassB, (ClassA, ClassB), Int]
  object ByRightHashJoiner extends ByRightHashJoin[ClassA, ClassB, (ClassA, ClassB), Int]

   implicit object TripleNestedLoopJoiner extends RegularNestedLoopJoin[(ClassA, ClassB), ClassC, (ClassA, ClassB, ClassC)]

  it should "work" in {
    val as= Array(ClassA(1, "avalue1"))
    val bs= Array(ClassB(1, "bvalue1"))
    val cs: Traversable[ClassC] = Vector(ClassC(1, "cvalue1"))
    
    val nestedLoopResult= NestedLoopJoiner.innerJoin(as, bs)
    val byLeftHashResult = ByLeftHashJoiner.innerJoin(as, bs)
    val byRightHashResult = ByRightHashJoiner.innerJoin(as, bs)
    println(nestedLoopResult)
    println(byLeftHashResult)
    println(byRightHashResult)
    
    val res3 = TripleNestedLoopJoiner.innerJoin(nestedLoopResult, cs)
    println(res3)
  }
  
  "NestedLoop Join Type class" should "work" in {
    import NestedLoopJoin._
    val as: Traversable[ClassA] = Vector(ClassA(1, "avalue1"))
    val bs: Traversable[ClassB] = Vector(ClassB(1, "bvalue1"))
    val cs: Traversable[ClassC] = Vector(ClassC(1, "cvalue1"))
//
//    implicit class InnerJoinOps (lr: (Traversable[ClassA], Traversable[ClassB]))(implicit jp:JoinPredicate[ClassA, ClassB], jc:InnerJoinCombinator[ClassA, ClassB, (ClassA,ClassB)], i:NestedLoopJoin[ClassA, ClassB, (ClassA,ClassB)]){
//      def innerJoin:Traversable[(ClassA, ClassB)] = {i.innerJoin(lr._1, lr._2)(jp, jc)}
//    }

    val res1 = (as, bs).innerJoin
    val res13 = (res1, cs).innerJoin

    println("Nested loop type class")
    println(res1)
    println(res13)
    
    val res2 = as |><| bs |><| cs
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

  it/*ignore*/ should "measure time" in {
    val iters = 2
    val aCount = 10000
    val bCount = 100000

    val as = for (key <- 1 to aCount)yield ClassA(key, s"avalue$key")
    val bs = for (key <- 1 to bCount)yield ClassB(key, s"bvalue$key")
    println("Gen finished")

    for(i <- 1 to iters){
      val start = System.currentTimeMillis()
      val result = ByLeftHashJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"ByLeftHashJoiner took ${end-start} ms, result size: ${result.size}")
    }
    
    for(i <- 1 to 0){
      val start = System.currentTimeMillis()
      val result = ByRightHashJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"ByRightHashJoiner took ${end-start} ms, result size: ${result.size}")
    }

    for(i <- 1 to iters){
      val start = System.currentTimeMillis()
      val result = NestedLoopJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"NestedLoopJoiner took ${end-start} ms, result size: ${result.size}")
    }
    object ParallelNestedLoopJoiner extends ParallelNestedLoopJoin[ClassA, ClassB, (ClassA, ClassB)]
    for(i <- 1 to iters){
      val start = System.currentTimeMillis()
      val result = ParallelNestedLoopJoiner.innerJoin(as, bs)
      val end = System.currentTimeMillis()
      println(s"ParallelNestedLoopJoiner took ${end-start} ms, result size: ${result.size}")
    }
  }
}
