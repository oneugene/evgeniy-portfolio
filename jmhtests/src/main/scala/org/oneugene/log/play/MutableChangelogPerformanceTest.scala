package org.oneugene.log.play

import java.util.concurrent.TimeUnit

import org.oneugene.log.model._
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class MutableChangelogPerformanceTest {

  import PrebuildLenses._

  class TestClass(initialUser: User) extends MutableChangeLog[User] {
    override protected var state: User = initialUser
  }

  @Benchmark
  def runModification(testValues: TestConstants, bh: Blackhole): Unit = {
    val service = new TestClass(testValues.originalUser)
    val modification = service
      .recordChange(nameOcLens.set(testValues.newName))
      .recordChange(birthDayOcLens.set(testValues.newDay))
      .recordChange(birthMonthOcLens.set(testValues.newMonth))
      .recordChange(birthYearOcLens.set(testValues.newYear))
      .run
    bh.consume(modification)
  }
}
