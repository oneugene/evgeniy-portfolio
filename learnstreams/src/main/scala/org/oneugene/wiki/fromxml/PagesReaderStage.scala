package org.oneugene.wiki.fromxml

import akka.stream.alpakka.xml._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import org.oneugene.wiki.model.Page

class PagesReaderStage extends GraphStage[FlowShape[ParseEvent, Page]] {
  val in: Inlet[ParseEvent] = Inlet("WikiPagesStage.in")
  val out: Outlet[Page] = Outlet("WikiPagesStage.out")
  override val shape: FlowShape[ParseEvent, Page] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler with OutHandler {
      private val skipReader = new TagSkipper
      private val pageReader = new PageReader

      private var state: ParseEvent => Unit = outsideRootTag;

      private def outsideRootTag(event: ParseEvent): Unit = {
        event match {
          case st: StartElement =>
            state = insideRootTag
          case other =>
        }
        pull(in)
      }

      private def insideRootTag(event: ParseEvent): Unit = {
        event match {
          case st: StartElement if st.localName == "page" =>
            state = readingPageTag
          case st: StartElement if st.localName != "page" =>
            state = skippingTag
          case en: EndElement =>
            state = outsideRootTag
          case other =>
        }
        pull(in)
      }

      private def readingPageTag(event: ParseEvent): Unit = {
        val receiveResult: ReceiveResult[Page] = pageReader.receive(event)
        receiveResult.fold(
          failStage,
          p => {
            state = insideRootTag;
            push(out, p)
          },
          pull(in))
      }

      private def skippingTag(event: ParseEvent): Unit = {
        val receiveResult: ReceiveResult[Unit] = skipReader.receive(event)
        receiveResult.fold(
          failStage,
          _ => {
            state = insideRootTag;
            pull(in)
          },
          pull(in)
        )
      }

      override def onPull(): Unit = pull(in)

      override def onPush(): Unit = {
        val event: ParseEvent = grab(in)
        state(event)
      }

      setHandlers(in, out, this)
    }

}
