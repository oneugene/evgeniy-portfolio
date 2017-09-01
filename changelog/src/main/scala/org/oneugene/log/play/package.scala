package org.oneugene.log

import scalaz.{LensFamily, Writer}

package object play {
  type ObjectChangeLens[S, A] = LensFamily[S, ObjectChangeRecord[S, A], A, A]
  type PropertyChangeLens[S, A] = LensFamily[S, Writer[Vector[String], S], A, Writer[Vector[String], A]]
}
