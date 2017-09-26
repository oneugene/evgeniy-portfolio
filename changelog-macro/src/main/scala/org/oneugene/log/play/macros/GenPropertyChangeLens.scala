package org.oneugene.log.play.macros

import org.oneugene.log.play.PropertyChangeLens

/**
  * An interface to macro which generates [[PropertyChangeLens]]
  *
  * @tparam S
  */
class GenPropertyChangeLens[S] {
  def apply[A](field: S => A): PropertyChangeLens[S, A] = macro GenPropertyChangeLensImpl.genLensImpl[S, A]
}

object GenPropertyChangeLens {
  def apply[S] = new GenPropertyChangeLens[S]
}
