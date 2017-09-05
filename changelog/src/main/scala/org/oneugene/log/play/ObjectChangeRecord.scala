package org.oneugene.log.play

import org.oneugene.log.PropertyChange

/**
  * Represents single change to the object
  *
  * @param changedValue object value after change applied
  * @param change property change
  * @tparam A type of the object the change has been recorded for
  * @tparam B type of the property which has been changed
  */
case class ObjectChangeRecord[+A, +B](changedValue: A, change: PropertyChange[B])
