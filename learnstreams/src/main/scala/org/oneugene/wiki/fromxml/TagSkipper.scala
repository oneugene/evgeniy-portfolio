package org.oneugene.wiki.fromxml

import akka.stream.alpakka.xml._

class TagSkipper extends XmlEventsAccumulator[Unit] {
  private var indent: Int = 0

  override def receive(event: ParseEvent): ReceiveResult[Unit] = {
    event match {
      case st: StartElement =>
        indent += 1
        ReceiveResult.incomplete
      case en: EndElement if indent > 0 =>
        indent -= 1
        ReceiveResult.incomplete
      case en: EndElement if indent == 0 =>
        ReceiveResult.complete(())
      case other =>
        ReceiveResult.incomplete
    }
  }
}
