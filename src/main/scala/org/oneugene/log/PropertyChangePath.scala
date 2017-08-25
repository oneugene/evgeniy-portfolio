package org.oneugene.log

import scalaz.Monoid

case class PropertyChangePath[+V](path: Seq[PropertyChange[V]])

trait PropertyChangePathInstances {
  implicit def propertyChangePathMonoid[V]: Monoid[PropertyChangePath[V]] = new Monoid[PropertyChangePath[V]] {
    // Vector concat is O(n^2) in Scala 2.10 - it's actually faster to do repeated appends
    // https://issues.scala-lang.org/browse/SI-7725
    //
    // It was reduced to O(n) in Scala 2.11 - ideally it would be O(log n)
    // https://issues.scala-lang.org/browse/SI-4442
    def append(f1: PropertyChangePath[V], f2: => PropertyChangePath[V]) = PropertyChangePath(f2.path.foldLeft(f1.path)(_ :+ _))

    def zero: PropertyChangePath[V] = PropertyChangePath(Vector.empty)
  }
}
