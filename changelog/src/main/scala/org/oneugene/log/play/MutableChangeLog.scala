package org.oneugene.log.play

import org.oneugene.log.PropertyChange

import scala.collection.mutable.ArrayBuffer

/**
  * A trait which can collect changes to the object in mutable way
  * @tparam A
  */
trait MutableChangeLog[A] {
  /**
    * Contains changes to properties of the object
    */
  private val changeLog = ArrayBuffer[PropertyChange[_]]()
  /**
    * Current state of the object
    */
  protected var state: A

  def recordChange(f: (A => ObjectChangeRecord[A, _])): this.type = {
    f.apply(state) match {
      case PropertyChangeRecord(changedValue, changeEntry) =>
        changeLog += changeEntry
        state = changedValue
      case NoChangesRecord() =>
    }
    this
  }

  def run: (A, Seq[PropertyChange[_]]) = state -> changeLog.toVector
}
