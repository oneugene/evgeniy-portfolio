package org.oneugene.join

import scala.collection.Traversable
import scala.collection.immutable.Vector
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * A function which compares 2 items from different collections when running nested loop join operation with [[org.oneugene.join.NestedLoopJoin]]
 * It should return `true` if the items match and `false` otherwise.
 *
 * @tparam L type of elements in `left` collection
 * @tparam R type of elements in `right` collection
 */
trait JoinPredicate[-L, -R] extends ((L, R) => Boolean) {
}

/**
 * A function which creates an object with defined `hashCode` and `equals` methods by an element in a collection.
 * Used by [[org.oneugene.join.HashJoin]] to build hash table which is used for comparison of items in collections
 *
 * @tparam A type of elements in a collection
 * @tparam K type of keys in hash table
 */
trait HashJoinKey[-A, +K] extends (A => K) {
}

/**
 * A function to combine 2 elements from collections when running `inner join` operation into elements of the collection with result
 *
 * @tparam L type of elements in `left` collection
 * @tparam R type of elements in `right` collection
 * @tparam J type of elements in collection with inner join results
 */
trait InnerJoinCombinator[-L, -R, +J] extends ((L, R) => J) {
}

/**
 * A function to combine 2 elements from collections when running `left outer join` operation into elements of the collection with result
 *
 * @tparam L type of elements in `left` collection
 * @tparam R type of elements in `right` collection
 * @tparam J type of elements in collection with left outer join results
 */
trait LeftOuterJoinCombinator[-L, -R, +J] extends ((L, Option[R]) => J) {
  //  def combine(l: L, r: Option[R]): J
}

/**
 * An implementation of [[org.oneugene.join.InnerJoinCombinator]] which creates `Tuple2` elements
 *
 * @tparam L type of elements in `left` collection
 * @tparam R type of elements in `right` collection
 */
class TupleInnerJoinCombinator[L, R] extends InnerJoinCombinator[L, R, (L, R)] {
  override def apply(l: L, r: R): (L, R) = (l, r)
}

/**
 * An implementation of [[org.oneugene.join.LeftOuterJoinCombinator]] which creates `Tuple2` elements
 *
 * @tparam L type of elements in `left` collection
 * @tparam R type of elements in `right` collection
 */
class TupleLeftOuterJoinCombinator[L, R] extends TupleInnerJoinCombinator[L, Option[R]] with LeftOuterJoinCombinator[L, R, (L, Option[R])]

/**
 * Describes hash inner join operation algorithm methods and parameters
 *
 * @tparam L type of elements in `left` collection
 * @tparam R type of elements in `right` collection
 * @tparam J type of elements in collection with inner join results
 * @tparam K type of keys in hash table
 */
trait HashJoin[L, R, J, K] {
  /**
   * A method for inner join with implicit parameters
   *
   * @return inner join result for 2 collections
   * @param left `left` collection
   * @param right `right` collection
   * @param lKey function to calculate hash table keys by `left` collection elements
   * @param rKey function to calculate hash table keys by `right` collection elements
   * @param c factory function to produce collection elements in result
   */
  def innerJoin(left: Traversable[L], right: Traversable[R])(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], c: InnerJoinCombinator[L, R, J]): Traversable[J] = {
    this.innerJoin(left, right, lKey, rKey, c);
  }

  /**
   * A method for inner join
   *
   * @return inner join result for 2 collections
   * @param left `left` collection
   * @param right `right` collection
   * @param lKey function to calculate hash table keys by `left` collection elements
   * @param rKey function to calculate hash table keys by `right` collection elements
   * @param c factory function to produce collection elements in result
   */
  def innerJoin(left: Traversable[L], right: Traversable[R], lKey: (L => K), rKey: (R => K), c: ((L, R) => J)): Traversable[J]
}

/**
 * Default implementation of hash inner join when hash table is built by `left` collection.
 *
 * Works good when `left` collection is smaller than `right` collection
 */
trait ByLeftHashJoin[L, R, J, K] extends HashJoin[L, R, J, K] {
  override def innerJoin(left: Traversable[L], right: Traversable[R], lKey: (L => K), rKey: (R => K), c: ((L, R) => J)): Traversable[J] = {
    val result = new ArrayBuffer[J](Math.min(left.size, right.size))

    val hash = new mutable.HashMap[K, mutable.Set[L]] with mutable.MultiMap[K, L]

    left.seq.foreach(l => hash.addBinding(lKey(l), l))

    right.seq.foreach({ r =>
      val rk = rKey(r)
      for {
        ls <- hash.get(rk)
        l <- ls
      } result += c(l, r)
    })
    result.toVector
  }
}

/**
 * Default implementation of hash inner join when hash table is built by `right` collection.
 *
 * Works good when `right` collection is smaller than `left` collection
 */
trait ByRightHashJoin[L, R, J, K] extends HashJoin[L, R, J, K] {
  override def innerJoin(left: Traversable[L], right: Traversable[R], lKey: (L => K), rKey: (R => K), c: ((L, R) => J)): Traversable[J] = {
    val result = new ArrayBuffer[J](Math.min(left.size, right.size))

    val hash = new mutable.HashMap[K, mutable.Set[R]] with mutable.MultiMap[K, R]

    right.seq.foreach(r => hash.addBinding(rKey(r), r))

    left.seq.foreach({ l =>
      val lk = lKey(l)
      for {
        rs <- hash.get(lk)
        r <- rs
      } result += c(l, r)
    })
    result.toVector
  }
}

/**
 * Describes nested loop join operation algorithm methods and parameters
 *
 * @tparam L type of elements in `left` collection
 * @tparam R type of elements in `right` collection
 * @tparam J type of elements in collection with inner join results
 */
trait NestedLoopJoin[L, R, J] {
  /**
   * A method for inner join with implicit parameters
   *
   * @return inner join result for 2 collections
   * @param left `left` collection
   * @param right `right` collection
   * @param jp function to check if elements in the collections match
   * @param c factory function to produce collection elements in result
   */
  def innerJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: InnerJoinCombinator[L, R, J]): Traversable[J] = {
    this.innerJoin(left, right, jp, c)
  }

  /**
   * A method for inner join
   *
   * @return inner join result for 2 collections
   * @param left `left` collection
   * @param right `right` collection
   * @param jp function to check if elements in the collections match
   * @param c factory function to produce collection elements in result
   */
  def innerJoin(left: Traversable[L], right: Traversable[R], jp: ((L, R) => Boolean), c: ((L, R) => J)): Traversable[J]

  /**
   * A method for left outer join with implicit parameters
   *
   * @return left outer join result for 2 collections
   * @param left `left` collection
   * @param right `right` collection
   * @param jp function to check if elements in the collections match
   * @param c factory function to produce collection elements in result
   */
  def leftOuterJoin(left: Traversable[L], right: Traversable[R])(implicit jp: JoinPredicate[L, R], c: LeftOuterJoinCombinator[L, R, J]): Traversable[J] = {
    this.leftOuterJoin(left, right, jp, c)
  }

  /**
   * A method for left outer join
   *
   * @return left outer join result for 2 collections
   * @param left `left` collection
   * @param right `right` collection
   * @param jp function to check if elements in the collections match
   * @param c factory function to produce collection elements in result
   */
  def leftOuterJoin(left: Traversable[L], right: Traversable[R], jp: ((L, R) => Boolean), c: ((L, Option[R]) => J)): Traversable[J]
}

/**
 * Default implementation of hash nested loop join
 */
trait RegularNestedLoopJoin[L, R, J] extends NestedLoopJoin[L, R, J] {

  override def leftOuterJoin(left: Traversable[L], right: Traversable[R], jp: ((L, R) => Boolean), c: ((L, Option[R]) => J)): Traversable[J] = {
    val result = new ArrayBuffer[J](Math.min(left.size, right.size))
    left.seq.foreach { l =>
      val check = result.size
      right.seq.foreach { r =>
        if (jp(l, r)) {
          result += c(l, Some(r))
        }
      }
      if (check == result.size) {
        result += c(l, None)
      }
    }
    result.toVector
  }

  override def innerJoin(left: Traversable[L], right: Traversable[R], jp: ((L, R) => Boolean), c: ((L, R) => J)): Traversable[J] = {
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

/**
 * Implementation of hash nested loop join using parallel collections
 */
trait ParallelNestedLoopJoin[L, R, J] extends NestedLoopJoin[L, R, J] {
  override def leftOuterJoin(left: Traversable[L], right: Traversable[R], jp: ((L, R) => Boolean), c: ((L, Option[R]) => J)): Traversable[J] = {
    val zero = Vector[J]()
    left.par.aggregate(zero)({ (acc: Vector[J], l) =>
      val partial = new ArrayBuffer[J]()
      right.foreach { r =>
        if (jp(l, r)) {
          partial += c(l, Some(r))
        }
      }
      if (partial.isEmpty) {
        partial += c(l, None)
        acc :+ c(l, None)
      }
      acc ++ partial
    }, { _ ++ _ })
  }

  override def innerJoin(left: Traversable[L], right: Traversable[R], jp: ((L, R) => Boolean), c: ((L, R) => J)): Traversable[J] = {
    val zero: Traversable[J] = Nil
    left.par.aggregate(zero)({ (acc, l) =>
      val partial = new ArrayBuffer[J]()
      right.foreach { r =>
        if (jp(l, r)) {
          partial += c(l, r)
        }
      }
      acc ++ partial
    }, { _ ++ _ })
  }
}

/**
 * Type class to nested loop join allows to write code less code e.g.
 * {{{
 * val a=List(1,2,3)
 * val b=List(2,3,4)
 *
 * val c = (a,b).innerJoin
 * }}}
 */
final class NestedLoopInnerJoinUnary[L, R, J](lr: (Traversable[L], Traversable[R]), jp: JoinPredicate[L, R], jc: InnerJoinCombinator[L, R, J], i: NestedLoopJoin[L, R, J]) {
  def innerJoin: Traversable[J] = i.innerJoin(lr._1, lr._2)(jp, jc)
}

/**
 * Type class to nested loop join allows to write code less code e.g.
 * {{{
 * val a=List(1,2,3)
 * val b=List(2,3,4)
 *
 * val c = a innerJoin b
 * val c = a leftOuterJoin b
 * }}}
 */
final class NestedLoopJoinBinary[L, R, J](l: Traversable[L]) {
  def innerJoin(r: Traversable[R])(implicit jp: JoinPredicate[L, R], jc: InnerJoinCombinator[L, R, J], i: NestedLoopJoin[L, R, J]): Traversable[J] = i.innerJoin(l, r)(jp, jc)
  def leftOuterJoin(r: Traversable[R])(implicit jp: JoinPredicate[L, R], jc: LeftOuterJoinCombinator[L, R, J], i: NestedLoopJoin[L, R, J]): Traversable[J] = i.leftOuterJoin(l, r)(jp, jc)
}

/**
 * Implicit declarations for syntax described in [[org.oneugene.join.NestedLoopInnerJoinUnary]] and [[org.oneugene.join.NestedLoopJoinBinary]]
 */
trait ToNestedLoopInnerJoinOps {
  implicit def toInnerJoinUnary[L, R, J](lr: (Traversable[L], Traversable[R]))(implicit jp: JoinPredicate[L, R], jc: InnerJoinCombinator[L, R, J], i: NestedLoopJoin[L, R, J]) = new NestedLoopInnerJoinUnary(lr, jp, jc, i)

  implicit def toInnerJoinBinary[L, R, J](l: Traversable[L]): NestedLoopJoinBinary[L, R, J] = new NestedLoopJoinBinary[L, R, J](l)
}
/**
 * Object for to import implicit methods from [[org.oneugene.join.ToNestedLoopInnerJoinOps]]
 */
object NestedLoopJoin extends ToNestedLoopInnerJoinOps

/**
 * Type class to hash loop join allows to write code less code e.g.
 * {{{
 * val a=List(1,2,3)
 * val b=List(2,3,4)
 *
 * val c = (a,b).innerJoin
 * }}}
 */
final class HashInnerJoinUnary[L, R, J, K](lr: (Traversable[L], Traversable[R]), lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], jc: InnerJoinCombinator[L, R, J], i: HashJoin[L, R, J, K]) {
  def innerJoin: Traversable[J] = { i.innerJoin(lr._1, lr._2)(lKey, rKey, jc) }
}

/**
 * Type class to hash loop join allows to write code less code e.g.
 * {{{
 * val a=List(1,2,3)
 * val b=List(2,3,4)
 *
 * val c = a innerJoin b
 * }}}
 */
final class HashInnerJoinBinary[L, R, J, K](l: Traversable[L]) {
  def innerJoin(r: Traversable[R])(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], jc: InnerJoinCombinator[L, R, J], i: HashJoin[L, R, J, K]): Traversable[J] = { i.innerJoin(l, r)(lKey, rKey, jc) }
}

/**
 * Implicit declarations for syntax described in [[org.oneugene.join.HashInnerJoinUnary]] and [[org.oneugene.join.HashInnerJoinBinary]]
 */
trait ToHashInnerJoinOps {
  implicit def toInnerJoinUnary[L, R, J, K](lr: (Traversable[L], Traversable[R]))(implicit lKey: HashJoinKey[L, K], rKey: HashJoinKey[R, K], jc: InnerJoinCombinator[L, R, J], i: HashJoin[L, R, J, K]) = new HashInnerJoinUnary(lr, lKey, rKey, jc, i)
  implicit def toInnerJoinBinary[L, R, J, K](l: Traversable[L]): HashInnerJoinBinary[L, R, J, K] = new HashInnerJoinBinary[L, R, J, K](l)
}

/**
 * Object for to import implicit methods from [[org.oneugene.join.ToHashInnerJoinOps]]
 */
object HashJoin extends ToHashInnerJoinOps

