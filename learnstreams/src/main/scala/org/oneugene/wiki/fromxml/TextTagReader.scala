package org.oneugene.wiki.fromxml

import akka.stream.alpakka.xml.{Comment, EndElement, ParseEvent, TextEvent}

class TextTagReader extends XmlEventsAccumulator[String] {
  private val buffer = new StringBuilder

  override def receive(event: ParseEvent): ReceiveResult[String] = {
    event match {
      case t: TextEvent =>
        buffer.append(t.text)
        ReceiveResult.incomplete
      case en: EndElement =>
        val text = buffer.toString
        buffer.clear
        ReceiveResult.complete(text)
      case c: Comment =>
        ReceiveResult.incomplete
      case other =>
        ReceiveResult.failure(new IllegalStateException(s"Unexpected event $other in text only tag"))
    }
  }
}
