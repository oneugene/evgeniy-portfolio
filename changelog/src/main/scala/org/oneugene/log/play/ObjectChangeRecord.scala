package org.oneugene.log.play

import org.oneugene.log.PropertyChange

case class ObjectChangeRecord[+A, +B](changedValue: A, change: PropertyChange[B])
