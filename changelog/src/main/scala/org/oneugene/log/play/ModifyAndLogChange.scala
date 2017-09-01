package org.oneugene.log.play

import org.oneugene.log.PropertyChange

import scala.collection.mutable.ArrayBuffer

trait ModifyAndLogChange[A] {
  private val changeLog = ArrayBuffer[PropertyChange[_]]()
  protected var state: A

  def recordChange(f: (A => ObjectChangeRecord[A, _])): this.type = {
    f.apply(state) match {
      case ObjectChangeRecord(changedValue, changeEntry) =>
        changeLog += changeEntry
        state = changedValue
    }
    this
  }

  def run: (A, Seq[PropertyChange[_]]) = state -> changeLog.toVector
}
