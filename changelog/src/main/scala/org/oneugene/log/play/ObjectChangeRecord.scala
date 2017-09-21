package org.oneugene.log.play

import org.oneugene.log.PropertyChange

sealed trait ObjectChangeRecord[+A, +B]

/**
  * Represents single change to the object
  *
  * @param changedValue object value after change applied
  * @param change       property change
  * @tparam A type of the object the change has been recorded for
  * @tparam B type of the property which has been changed
  */
final case class PropertyChangeRecord[+A, +B](changedValue: A, change: PropertyChange[B]) extends ObjectChangeRecord[A, B]

final case class NoChangesRecord() extends ObjectChangeRecord[Nothing, Nothing]
