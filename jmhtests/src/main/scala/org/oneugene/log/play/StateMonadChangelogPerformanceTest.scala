package org.oneugene.log.play

import java.util.concurrent.TimeUnit

import org.oneugene.log.model._
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class StateMonadChangelogPerformanceTest {

  import PrebuildLenses._

  import scalaz.{State => ZState}

  @Benchmark
  def runModificationWithScalaz(testValues: TestConstants, bh: Blackhole): Unit = {
    val program: ZState[ObjectChangelog[User], Unit] = constructScalazProgram(testValues)
    val modification = program.exec(ObjectChangelog.empty(testValues.originalUser))
    bh.consume(modification)
  }

  def constructScalazProgram(testValues: TestConstants): ZState[ObjectChangelog[User], Unit] = {
    import ZState.{init, modify}

    val nameChange: (ObjectChangelog[User]) => ObjectChangelog[User] = nameClLens.set(testValues.newName)
    val birthDayChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthDayClLens.set(testValues.newDay)
    val birthMonthChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthMonthClLens.set(testValues.newMonth)
    val birthYearChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthYearClLens.set(testValues.newYear)
    for {
      _ <- init
      _ <- modify(nameChange)
      _ <- modify(birthDayChange)
      _ <- modify(birthMonthChange)
      _ <- modify(birthYearChange)
    } yield ()
  }

  import cats.data.{State => CState}

  @Benchmark
  def runModificationWithCats(testValues: TestConstants, bh: Blackhole): Unit = {
    val program: CState[ObjectChangelog[User], Unit] = constructCatsProgram(testValues)
    val modification = program.runS(ObjectChangelog.empty(testValues.originalUser)).value
    bh.consume(modification)
  }

  def constructCatsProgram(testValues: TestConstants): CState[ObjectChangelog[User], Unit] = {
    import CState.{get, modify}
    val nameChange: (ObjectChangelog[User]) => ObjectChangelog[User] = nameClLens.set(testValues.newName)
    val birthDayChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthDayClLens.set(testValues.newDay)
    val birthMonthChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthMonthClLens.set(testValues.newMonth)
    val birthYearChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthYearClLens.set(testValues.newYear)
    for {
      _ <- get
      _ <- modify(nameChange)
      _ <- modify(birthDayChange)
      _ <- modify(birthMonthChange)
      _ <- modify(birthYearChange)
    } yield ()
  }
}
