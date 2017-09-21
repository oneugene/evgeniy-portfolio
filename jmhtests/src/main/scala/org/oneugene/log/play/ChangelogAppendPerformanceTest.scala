package org.oneugene.log.play

import java.util.concurrent.TimeUnit

import org.oneugene.log.model._
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class ChangelogAppendPerformanceTest {

  import org.oneugene.log.play.PrebuildLenses._

  @Benchmark
  def runModification(testValues: TestConstants, bh: Blackhole): Unit = {
    val modification: ObjectChangelog[User] = ObjectChangelog.empty(testValues.originalUser)
      .appendChange(nameOcLens.set(testValues.newName))
      .appendChange(birthDayOcLens.set(testValues.newDay))
      .appendChange(birthMonthOcLens.set(testValues.newMonth))
      .appendChange(birthYearOcLens.set(testValues.newYear))
    bh.consume(modification)
  }

}
