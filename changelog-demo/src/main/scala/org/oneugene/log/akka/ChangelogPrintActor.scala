package org.oneugene.log.akka

import akka.actor.{Actor, Props}
import akka.event.{Logging, LoggingAdapter}
import org.oneugene.log.PropertyChange

/**
  * Simple actor which logs changelog events
  */
class ChangelogPrintActor extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)

  override def receive = {
    case a: PropertyChange[_] => log.info(s"Received property change: $a")
  }
}

object ChangelogPrintActor {
  def props: Props = Props[ChangelogPrintActor]
}
