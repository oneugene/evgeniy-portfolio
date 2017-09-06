package org.oneugene.parsers

import _root_.java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit}
import org.openjdk.jmh.infra.Blackhole

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class ScalaParserPerformanceTest {

  @Benchmark
  def parsePerformance(testValues: TestConstants, bh: Blackhole): Unit = {
    val result = ScalaSearchStringParser.expression(testValues.queryReader)
    bh.consume(result)
  }
}
