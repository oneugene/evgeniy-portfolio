package org.oneugene.log

import org.oneugene.log.UserContainer.BDateDay

import scalaz._

case class PropertyChange[+V](replay: PropertyChangeReplay[BDateDay], changedValue: V)

trait PropertyChangeReplay[A] extends PartialFunction[A, A]

trait PropertyChangeDescription[F[_]] extends Monoid[F] {
  self =>
  def point[A](a: => A): F[A]

  // alias for point
  final def pure[A](a: => A): F[A] = point(a)
}

/*

case class PropertyChangeMonoid[V](newValue: V) extends Monoid[PropertyChange[V]]{
  override def zero = PropertyChange(Vector(), newValue)

  override def append(f1: PropertyChange[V], f2: => PropertyChange[V]) = ???
}
*/
