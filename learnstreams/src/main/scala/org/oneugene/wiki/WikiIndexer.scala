package org.oneugene.wiki

import java.nio.file.Paths

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.xml.ParseEvent
import akka.stream.alpakka.xml.scaladsl.XmlParsing
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import org.oneugene.wiki.fromxml.PagesReaderStage
import org.oneugene.wiki.model.Page

import scala.concurrent.Future

object WikiIndexer extends App {

  implicit val system: ActorSystem = ActorSystem("WikiIndexer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val file = Paths.get("D:\\enwiki-20171001-pages-articles1.xml-p10p30302")
//  val file = Paths.get("D:\\enwiki-20171001-stub-articles1.xml")

  val fileReader: Source[ByteString, Future[IOResult]] = FileIO.fromPath(file)

  val xmlEvents: Source[Page, Future[IOResult]] = fileReader
    .via(XmlParsing.parser)
    .via(XmlParsing.coalesce(Int.MaxValue))
    .via(Flow.fromGraph(new PagesReaderStage()))

  var start = System.currentTimeMillis()
  val result: Future[Done] = xmlEvents.runWith(Sink.foreach(_ => ()))
//  val result: Future[Done] = xmlEvents.runWith(Sink.foreach(println))

  result.onComplete(_ => {
    system.terminate()
    println(s"Took ${System.currentTimeMillis() - start} ms")
  })(system.dispatcher)
}
