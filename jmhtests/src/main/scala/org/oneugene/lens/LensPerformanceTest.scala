package org.oneugene.lens

import java.util.concurrent.TimeUnit

import org.oneugene.log.play.TestConstants
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit}
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class LensPerformanceTest {
  @Benchmark
  def runScalazLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.nameZLens.set(testValues.originalUser, testValues.newName))
  }

  @Benchmark
  def runComposedScalazLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.birthDayZLens.set(testValues.originalUser, testValues.newDay))
  }

  @Benchmark
  def runMyLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.nameMyLens.set(testValues.originalUser, testValues.newName))
  }

  @Benchmark
  def runComposedMyLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.birthDayMyLens.set(testValues.originalUser, testValues.newDay))
  }

  @Benchmark
  def runMonocleLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.nameMLens.set(testValues.newName)(testValues.originalUser))
  }

  @Benchmark
  def runComposedMonocleLens(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(LensesContainer.birthDayMLens.set(testValues.newDay)(testValues.originalUser))
  }
}
