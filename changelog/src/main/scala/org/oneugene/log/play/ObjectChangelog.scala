package org.oneugene.log.play

import org.oneugene.log.PropertyChange

/**
  * Immutable object changelog
  *
  * @param currentValue current value of the object
  * @param changeLog    changes that were made to the object
  * @tparam A
  */
case class ObjectChangelog[A](currentValue: A, changeLog: Vector[PropertyChange[_]]) {
  def appendChange(f: A => ObjectChangeRecord[A, _]): ObjectChangelog[A] = {
    val change = f(this.currentValue)
    change match {
      case PropertyChangeRecord(changedValue, propertyChange) =>
        ObjectChangelog(changedValue, this.changeLog :+ propertyChange)
      case NoChangesRecord() =>
        this
    }
  }
}

object ObjectChangelog {
  def empty[A](a: A): ObjectChangelog[A] = ObjectChangelog(a, Vector.empty)
}
