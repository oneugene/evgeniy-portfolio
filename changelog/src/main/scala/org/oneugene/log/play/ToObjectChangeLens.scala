package org.oneugene.log.play

import org.oneugene.log.PropertyChange

import scalaz.{LensFamily, Writer}

final class ObjectChangeLensConv[S, A](val self: PropertyChangeLens[S, A]) extends AnyVal {

  import scalaz.Scalaz._

  def objectChangeLens: ObjectChangeLens[S, A] = {
    LensFamily.lensFamilyu[S, ObjectChangeRecord[S, A], A, A]((sourceObject, valueToSet) => {
      val changeWitPath: Writer[Vector[String], S] = self.set(sourceObject, valueToSet.set(Vector.empty))
      val (path, modifiedObject) = changeWitPath.run
      ObjectChangeRecord(modifiedObject, PropertyChange(path, valueToSet, self.get(sourceObject)))
    }, self.get)
  }
}

trait ToObjectChangeLens {
  implicit def ToObjectChangeLens[S, A](a: PropertyChangeLens[S, A]): ObjectChangeLensConv[S, A] = new ObjectChangeLensConv(a)
}

object ObjectChangeLens extends ToObjectChangeLens
