package org.oneugene.log.play

import java.util.concurrent.TimeUnit

import org.oneugene.log.model.{BDate, BDateDay, User}
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit}
import org.openjdk.jmh.infra.Blackhole

import scalaz.Lens

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class ScalazLensPerformanceTest {
  @Benchmark
  def runScalazLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.nameLens.set(testValues.originalUser, testValues.newName))
  }

  @Benchmark
  def runComposedScalazLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.birthDayLens.set(testValues.originalUser, testValues.newDay))
  }

  @Benchmark
  def runMyLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.nameMyLens.set(testValues.originalUser, testValues.newName))
  }

  @Benchmark
  def runComposedMyLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.birthDayMyLens.set(testValues.originalUser, testValues.newDay))
  }

}

case class MyLens[S, T, A, B](set: (S, B) => T, get: S => A) {
  def <=<[C1, C2](that: MyLens[C1, C2, S, T]): MyLens[C1, C2, A, B] = {
    MyLens((c1, b) => that.set(c1, this.set(that.get(c1), b)), (c1) => this.get(that.get(c1)))
  }

  def >=>[C1, C2](that: MyLens[A, B, C1, C2]): MyLens[S, T, C1, C2] = that <=< this
}

object LensesContainer {
  val nameLens: Lens[User, String] = Lens.lensu[User, String]((user, name) => user.copy(name = name), _.name)
  val birthDateLens: Lens[User, BDate] = Lens.lensu[User, BDate]((user, bdate) => user.copy(birthDate = bdate), _.birthDate)
  val dayLens: Lens[BDate, BDateDay] = Lens.lensu[BDate, BDateDay]((date, day) => date.copy(day = day), _.day)

  val birthDayLens: Lens[User, BDateDay] = dayLens <=< birthDateLens


  val nameMyLens: MyLens[User, User, String, String] = MyLens[User, User, String, String]((user, name) => user.copy(name = name), _.name)
  val birthDateMyLens: MyLens[User, User, BDate, BDate] = MyLens[User, User, BDate, BDate]((user, bdate) => user.copy(birthDate = bdate), _.birthDate)
  val dayMyLens: MyLens[BDate, BDate, BDateDay, BDateDay] = MyLens[BDate, BDate, BDateDay, BDateDay]((date, day) => date.copy(day = day), _.day)

  val birthDayMyLens: MyLens[User, User, BDateDay, BDateDay] = dayMyLens <=< birthDateMyLens
}
