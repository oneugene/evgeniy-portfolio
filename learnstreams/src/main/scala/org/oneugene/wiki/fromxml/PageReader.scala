package org.oneugene.wiki.fromxml

import akka.stream.alpakka.xml.{EndElement, ParseEvent, StartElement}
import org.oneugene.wiki.model.Page

class PageReader extends XmlEventsAccumulator[Page] {
  private val skipReader = new TagSkipper
  private val capturedTitle: CaptureResultEventsAccumulator[String] = new CaptureResultEventsAccumulator[String](new TextTagReader)
  private val capturedId: CaptureResultEventsAccumulator[Long] = new CaptureResultEventsAccumulator[Long](new LongTagReader)

  private var currentSubReader: Option[XmlEventsAccumulator[Unit]] = Option.empty

  override def receive(event: ParseEvent): ReceiveResult[Page] = {
    //    println(s"Bevent:$event, subState:$currentSubReader, title=$title, id=$id")
    val r = currentSubReader.fold(processEvent(event))(delegateToSubState(event, _))
    //    println(s"Aevent:$event, subState:$currentSubReader, title=$title, id=$id")
    //    println(r)
    //    println()
    r
  }

  private def processEvent(event: ParseEvent): ReceiveResult[Page] = {
    event match {
      case en: EndElement if en.localName == "page" =>
        capturedId.retrieve.map(id => Page(capturedTitle.retrieve.fold(_ => "", identity, ""), id))
      case en: EndElement if en.localName != "page" =>
        ReceiveResult.failure(new IllegalStateException(s"Expected end element </page> but got $en"))
      case st: StartElement if st.localName == "title" =>
        currentSubReader = Some(capturedTitle)
        ReceiveResult.incomplete
      case st: StartElement if st.localName == "id" =>
        currentSubReader = Some(capturedId)
        ReceiveResult.incomplete
      case st: StartElement =>
        currentSubReader = Some(skipReader)
        ReceiveResult.incomplete
      case other =>
        ReceiveResult.incomplete
    }
  }

  private def delegateToSubState(event: ParseEvent, subState: XmlEventsAccumulator[Unit]): ReceiveResult[Page] = {
    subState.receive(event).flatMap(_ => {
      currentSubReader = Option.empty; ReceiveResult.incomplete
    })
  }
}
