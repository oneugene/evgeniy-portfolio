package org.oneugene.lens

case class MyLens[S, T, A, B](set: (S, B) => T, get: S => A) {
  def <=<[C1, C2](that: MyLens[C1, C2, S, T]): MyLens[C1, C2, A, B] = {
    MyLens((c1, b) => that.set(c1, this.set(that.get(c1), b)), (c1) => this.get(that.get(c1)))
  }

  def >=>[C1, C2](that: MyLens[A, B, C1, C2]): MyLens[S, T, C1, C2] = that <=< this
}
