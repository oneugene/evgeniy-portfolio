package org.oneugene.wiki.fromxml

import akka.stream.alpakka.xml.ParseEvent

class CaptureResultEventsAccumulator[A](val delegate: XmlEventsAccumulator[A]) extends XmlEventsAccumulator[Unit] {
  private var capturedResult: ReceiveResult[A] = ReceiveResult.incomplete

  override def receive(event: ParseEvent): ReceiveResult[Unit] = {
    capturedResult = delegate.receive(event)
    capturedResult.map(_ => ())
  }

  def retrieve: ReceiveResult[A] = {
    capturedResult
  }
}
