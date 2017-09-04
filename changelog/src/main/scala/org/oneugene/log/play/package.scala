package org.oneugene.log

import scalaz.{LensFamily, Writer, State}

package object play {
  type PropertyChangeLens[S, A] = LensFamily[S, Writer[Vector[String], S], A, Writer[Vector[String], A]]

  type ObjectChangeLens[S, A] = LensFamily[S, ObjectChangeRecord[S, A], A, A]
  type ObjectChangelogLens[S, A] = LensFamily[ObjectChangelog[S], ObjectChangelog[S], A, A]
  //S = ObjectChangeLogState[S]
  //T = State[ObjectChangeLogState[S], ObjectChangeLogState[S]]
  //A = A
  //B = A

}
