package org.oneugene.log

import scalaz.{LensFamily, Writer}

package object play {
  /**
    * Shortcut type for lens which is focused on a property of the object
    * and it records property path to the property that has been changed.
    *
    * This lens could be easily composed with the other lenses of this type.
    *
    * @tparam S type of source object
    * @tparam A type of property the lens focused to
    */
  type PropertyChangeLens[S, A] = LensFamily[S, Writer[Vector[String], S], A, Writer[Vector[String], A]]

  /**
    * Shortcut type for lens which is focused on a property of the object and
    * it changes result type of modification to wrap modified object together with property change record
    *
    * This lens cannot be easily composed with the other lenses of this type.
    * It works good as terminal lens of transformation
    *
    * @tparam S type of source object
    * @tparam A type of property the lens focused to
    */
  type ObjectChangeLens[S, A] = LensFamily[S, ObjectChangeRecord[S, A], A, A]

  /**
    * Shortcut type for lens which is focused on a property of the object and
    * it works with changelog wrapper around object appending changes of the change logs
    * in addition to changing the property of the object
    *
    * This lens cannot be easily composed with the other lenses of this type.
    * It works good as terminal lens of transformation
    *
    * @tparam S type of source object
    * @tparam A type of property the lens focused to
    */
  type ObjectChangelogLens[S, A] = LensFamily[ObjectChangelog[S], ObjectChangelog[S], A, A]

}
