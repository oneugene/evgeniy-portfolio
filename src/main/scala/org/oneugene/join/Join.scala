package org.oneugene.join


import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.Traversable
import scala.collection.immutable.Vector

trait JoinPredicate[-L, -R] extends ((L, R) => Boolean){
  //def test
}

trait HashJoinKey[-A, +K] extends (A=>K) {
//def key
}

trait InnerJoinCombinator[-L, -R, +J] extends ((L,R)=>J){
}

trait LeftOuterJoinCombinator[-L, -R, +J] extends ((L, Option[R])=>J){
//  def combine(l: L, r: Option[R]): J
}

class TupleInnerJoinCombinator[L, R] extends InnerJoinCombinator[L, R, (L, R)] {
  override def apply(l: L, r: R): (L, R) = (l, r)
}

class TupleLeftOuterJoinCombinator[L, R] extends TupleInnerJoinCombinator[L, Option[R]] with LeftOuterJoinCombinator[L, R, (L, Option[R])]

trait HashJoin[L,R,J,K]{
  def innerJoin(left: Traversable[L], right: Traversable[R])(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], c: InnerJoinCombinator[L, R, J]): Traversable[J]
}

trait ByLeftHashJoin[L,R,J,K] extends HashJoin[L,R,J,K]{
  override def innerJoin(left: Traversable[L], right: Traversable[R])(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], c: InnerJoinCombinator[L, R, J]): Traversable[J] = {
    val result = new ArrayBuffer[J](Math.min(left.size, right.size))

    val hash = new mutable.HashMap[K, mutable.Set[L]] with mutable.MultiMap[K, L]

    left.seq.foreach(l => hash.addBinding(lKey(l), l))

    right.seq.foreach({r=>
      val rk = rKey(r)
      for {
        ls <- hash.get(rk)
        l <- ls
      } result+= c(l, r)
    })
    result.toVector
  }
}

trait ByRightHashJoin[L,R,J,K] extends HashJoin[L,R,J,K]{
  override def innerJoin(left: Traversable[L], right: Traversable[R])(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], c: InnerJoinCombinator[L, R, J]): Traversable[J] = {
    val result = new ArrayBuffer[J](Math.min(left.size, right.size))

    val hash = new mutable.HashMap[K, mutable.Set[R]] with mutable.MultiMap[K, R]

    right.seq.foreach(r => hash.addBinding(rKey(r), r))

    left.seq.foreach({l=>
      val lk = lKey(l)
      for {
        rs <- hash.get(lk)
        r <- rs
      } result+= c(l, r)
    })
    result.toVector
  }
}

trait NestedLoopJoin[L, R, J] {
  def innerJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: InnerJoinCombinator[L, R, J]): Traversable[J]
  def leftOuterJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: LeftOuterJoinCombinator[L, R, J]): Traversable[J]
}

trait RegularNestedLoopJoin[L, R, J] extends NestedLoopJoin[L, R, J] {

  override def leftOuterJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: LeftOuterJoinCombinator[L, R, J]): Traversable[J] = {
    val result = new ArrayBuffer[J](Math.min(left.size, right.size))
    left.seq.foreach { l =>
      val check = result.size
      right.seq.foreach { r =>
        if (jp(l, r)) {
          result += c(l, Some(r))
        }
      }
      if(check == result.size) {
        result += c(l, None)
      }
    }
    result.toVector
  }

  override def innerJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: InnerJoinCombinator[L, R, J]): Traversable[J] = {
    val result = new ArrayBuffer[J](Math.min(left.size, right.size))
    left.seq.foreach { l =>
      right.seq.foreach { r =>
        if (jp(l, r)) {
          result += c(l, r)
        }
      }
    }
    result.toVector
//    for{
//      l<- left
//      r<- right
//      if(jp(l, r))
//    }yield c(l, r)

//    left.flatMap { l => 
//      right.withFilter{jp(l, _)}.map { c(l, _) }
//    }
  }
}

trait ParallelNestedLoopJoin[L, R, J] extends NestedLoopJoin[L, R, J] {
  override def leftOuterJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: LeftOuterJoinCombinator[L, R, J]): Traversable[J] = {
    val zero = Vector[J]()
    left.par.aggregate(zero)( { (acc: Vector[J], l) =>
      val partial= new ArrayBuffer[J]()
      right.foreach { r =>
        if (jp(l, r)) {
          partial += c(l, Some(r))
        }
      }
      if(partial.isEmpty) {
        partial += c(l, None)
        acc :+ c(l, None)
      }
      acc ++ partial
    }, {_ ++ _})
  }

  override def innerJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: InnerJoinCombinator[L, R, J]): Traversable[J] = {
    val zero:Traversable[J] = Nil
    left.par.aggregate(zero)( { (acc, l) =>
      val partial= new ArrayBuffer[J]()
      right.foreach { r =>
        if (jp(l, r)) {
          partial += c(l, r)
        }
      }
      acc ++ partial
    }, {_ ++ _})
  }
}

final class NestedLoopInnerJoinUnary[L,R, J](lr: (Traversable[L], Traversable[R]), jp:JoinPredicate[L, R], jc:InnerJoinCombinator[L, R, J], i:NestedLoopJoin[L, R, J]){
  def innerJoin:Traversable[J] = i.innerJoin(lr._1, lr._2)(jp, jc)
}
final class NestedLoopJoinBinary[L, R, J](l:Traversable[L]){
  def innerJoin(r:Traversable[R])(implicit jp:JoinPredicate[L, R], jc:InnerJoinCombinator[L, R, J], i:NestedLoopJoin[L, R, J]):Traversable[J]= i.innerJoin(l, r)(jp, jc)
  def |><|(r:Traversable[R])(implicit jp:JoinPredicate[L, R], jc:InnerJoinCombinator[L, R, J], i:NestedLoopJoin[L, R, J]):Traversable[J]= i.innerJoin(l, r)(jp, jc)
  def leftOuterJoin(r:Traversable[R])(implicit jp:JoinPredicate[L, R], jc:LeftOuterJoinCombinator[L, R, J], i:NestedLoopJoin[L, R, J]):Traversable[J]= i.leftOuterJoin(l, r)(jp, jc)
  def =|><|(r:Traversable[R])(implicit jp:JoinPredicate[L, R], jc:LeftOuterJoinCombinator[L, R, J], i:NestedLoopJoin[L, R, J]):Traversable[J]= i.leftOuterJoin(l, r)(jp, jc)
}
trait ToNestedLoopInnerJoinOps {
  implicit def toInnerJoinUnary[L,R, J] (lr: (Traversable[L], Traversable[R]))(implicit jp:JoinPredicate[L, R], jc:InnerJoinCombinator[L, R, J], i:NestedLoopJoin[L, R, J]) = new NestedLoopInnerJoinUnary(lr, jp, jc, i)

  implicit def toInnerJoinBinary[L, R, J](l:Traversable[L]):NestedLoopJoinBinary[L, R, J] = new NestedLoopJoinBinary[L, R, J](l)
}

object NestedLoopJoin extends ToNestedLoopInnerJoinOps


final class HashInnerJoinUnary[L, R, J, K](lr: (Traversable[L], Traversable[R]), lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], jc:InnerJoinCombinator[L, R, J], i:HashJoin[L, R, J, K]){
  def innerJoin:Traversable[J] = {i.innerJoin(lr._1, lr._2)(lKey, rKey, jc)}
}

final class HashInnerJoinBinary[L, R, J, K](l: Traversable[L]){
  def innerJoin(r: Traversable[R])(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], jc:InnerJoinCombinator[L, R, J], i:HashJoin[L, R, J, K]):Traversable[J] = {i.innerJoin(l, r)(lKey, rKey, jc)}
  def |><|(r: Traversable[R])(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], jc:InnerJoinCombinator[L, R, J], i:HashJoin[L, R, J, K]):Traversable[J] = {i.innerJoin(l, r)(lKey, rKey, jc)}
}

trait ToHashInnerJoinOps {
  implicit def toInnerJoinUnary[L,R,J,K] (lr: (Traversable[L], Traversable[R]))(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], jc:InnerJoinCombinator[L, R, J], i:HashJoin[L, R, J, K]) = new HashInnerJoinUnary(lr, lKey, rKey, jc, i)
  implicit def toInnerJoinBinary[L,R,J,K] (l: Traversable[L]):HashInnerJoinBinary[L,R,J,K] = new HashInnerJoinBinary[L,R,J,K](l)
}

object HashJoin extends ToHashInnerJoinOps

