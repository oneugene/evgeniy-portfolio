package org.oneugene.log.play

import java.time.Month

import org.oneugene.log.model.{BDate, User}
import org.openjdk.jmh.annotations.{Scope, State}

@State(Scope.Benchmark)
class TestConstants {
  var originalUser = User("Ievgenii", BDate(1978, Month.OCTOBER, 3))
  var newName = "Test"
  var newYear = 1990
  var newMonth: Month = Month.APRIL
  var newDay = 20

}
