package org.oneugene.log.play

import org.oneugene.log.PropertyChange

case class ObjectChangelog[A](currentValue: A, changeLog: Vector[PropertyChange[_]]) {
  def appendChange(f: A => ObjectChangeRecord[A, _]): ObjectChangelog[A] = {
    val change = f(this.currentValue)
    ObjectChangelog(change.changedValue, this.changeLog :+ change.change)
  }
}

object ObjectChangelog {
  def empty[A](a: A): ObjectChangelog[A] = ObjectChangelog(a, Vector.empty)
}
