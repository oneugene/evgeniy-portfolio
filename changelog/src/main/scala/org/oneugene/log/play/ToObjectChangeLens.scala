package org.oneugene.log.play

import org.oneugene.log.PropertyChange

import scalaz.Writer

final class ObjectChangeLensConversions[S, A](val self: PropertyChangeLens[S, A]) extends AnyVal {

  import monocle.PLens

  import scalaz.Scalaz._

  /**
    * Converts PropertyChangeLens to ObjectChangeLens (terminal lens form)
    */
  def objectChangeLens: ObjectChangeLens[S, A] = {
    PLens[S, ObjectChangeRecord[S, A], A, A](self.get)((valueToSet) => (sourceObject) => {
      val changeWitPath: Writer[Vector[String], S] = self.set(valueToSet.set(Vector.empty))(sourceObject)
      val (path, modifiedObject) = changeWitPath.run
      if (isIdentityChange(path, sourceObject, modifiedObject)) {
        NoChangesRecord()
      } else {
        PropertyChangeRecord(modifiedObject, PropertyChange(path, valueToSet, self.get(sourceObject)))
      }
    })
  }

  /**
    * Converts PropertyChangeLens to ObjectChangelogLens (terminal lens form)
    */
  def objectChangelogLens: ObjectChangelogLens[S, A] = {
    PLens[ObjectChangelog[S], ObjectChangelog[S], A, A]((state) => self.get(state.currentValue))((valueToSet) => (state) => {
      val sourceObject = state.currentValue
      val changeWitPath: Writer[Vector[String], S] = self.set(valueToSet.set(Vector.empty))(sourceObject)
      val (path, modifiedObject) = changeWitPath.run
      if (isIdentityChange(path, sourceObject, modifiedObject)) {
        state
      } else {
        ObjectChangelog(modifiedObject, state.changeLog :+ PropertyChange(path, valueToSet, self.get(sourceObject)))
      }
    })
  }

  @inline private def isIdentityChange[A](path: Vector[String], originalObject: A, modifiedObject: A): Boolean =
    path.isEmpty && originalObject == modifiedObject
}

trait ToObjectChangeLens {
  implicit def ToObjectChangeLens[S, A](a: PropertyChangeLens[S, A]): ObjectChangeLensConversions[S, A] = new ObjectChangeLensConversions(a)
}

object ObjectChangeLens extends ToObjectChangeLens
