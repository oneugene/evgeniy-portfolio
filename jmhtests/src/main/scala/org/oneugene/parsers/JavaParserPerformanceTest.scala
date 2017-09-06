package org.oneugene.parsers

import _root_.java.util.concurrent.TimeUnit

import org.codehaus.jparsec.Parser
import org.oneugene.parsers.java.DefaultSearchStringParserFactory
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Array(Mode.AverageTime))
class JavaParserPerformanceTest {

  val expressionParser: Parser[java.Predicate] = new DefaultSearchStringParserFactory().expression

  @Benchmark
  def parsePerformance(testValues: TestConstants, bh: Blackhole): Unit = {
    bh.consume(expressionParser.parse(testValues.query))
  }
}
