package org.oneugene.parsers

import org.openjdk.jmh.annotations.{Scope, State}

import scala.util.parsing.input.CharSequenceReader

@State(Scope.Benchmark)
class TestConstants {
  var query =
    """
                  q = "1" AND c<="2" OR r>="3" AND u BETWEEN ("4", "5") AND (qwe <> "tryrty" OR l45 <= "asd")
      """
  var queryReader = new CharSequenceReader(query)
}
