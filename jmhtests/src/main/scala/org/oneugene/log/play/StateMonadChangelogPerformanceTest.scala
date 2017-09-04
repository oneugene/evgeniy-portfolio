package org.oneugene.log.play

import java.util.concurrent.TimeUnit

import org.oneugene.log.model._
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class StateMonadChangelogPerformanceTest {

  import PrebuildLenses._

  import scalaz.State
  import scalaz.State.{init, modify}

  @Benchmark
  def runModification(testValues: TestConstants, bh: Blackhole): Unit = {
    val program: State[ObjectChangelog[User], Unit] = constructProgram(testValues)
    val modification = program.exec(ObjectChangelog.empty(testValues.originalUser))
    bh.consume(modification)
  }

  def constructProgram(testValues: TestConstants): State[ObjectChangelog[User], Unit] = {
    val nameChange: (ObjectChangelog[User]) => ObjectChangelog[User] = nameClLens.set(_, testValues.newName)
    val birthDayChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthDayClLens.set(_, testValues.newDay)
    val birthMonthChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthMonthClLens.set(_, testValues.newMonth)
    val birthYearChange: (ObjectChangelog[User]) => ObjectChangelog[User] = birthYearClLens.set(_, testValues.newYear)
    for {
      _ <- init
      _ <- modify(nameChange)
      _ <- modify(birthDayChange)
      _ <- modify(birthMonthChange)
      _ <- modify(birthYearChange)
    } yield ()
  }
}
