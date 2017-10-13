package org.oneugene.wiki.fromxml

import akka.stream.alpakka.xml.ParseEvent

import scala.util.Try

class LongTagReader extends XmlEventsAccumulator[Long] {
  private val delegate = new TextTagReader

  override def receive(event: ParseEvent): ReceiveResult[Long] = {
    delegate.receive(event).flatMap(text => Try(text.toLong).fold(ReceiveResult.failure, ReceiveResult.complete[Long]))
  }
}
