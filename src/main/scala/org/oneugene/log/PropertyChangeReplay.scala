package org.oneugene.log

import scalaz._

trait PropertyChangeReplay[A] extends PartialFunction[A, A]

case class LensBasedPropertyChangeReplay[A, B](newValue: B, lens: Lens[A, B]) extends PropertyChangeReplay[A] {
  override def isDefinedAt(x: A): Boolean = true

  override def apply(v1: A): A = lens.set(v1, newValue)
}

case class PreviousValueAwareLensBasedPropertyChangeReplay[A, B](oldValue: B, newValue: B, lens: Lens[A, B]) extends PropertyChangeReplay[A] {
  override def isDefinedAt(x: A): Boolean = lens.get(x) == oldValue

  override def apply(v1: A): A = lens.set(v1, newValue)
}
