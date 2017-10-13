package org.oneugene.wiki.fromxml

import akka.stream.alpakka.xml.ParseEvent

trait XmlEventsAccumulator[+R] {
  /**
    * Receives event accumulates it's information within state.
    * Returns an option with result in the case if all required information has been collected and the state finished it's work
    * Returns an empty option (with side effect of pulling next event from upstream) in the case if state is still open and more events
    * required to close the state
    * @param event
    * @return
    */
  def receive(event:ParseEvent):ReceiveResult[R]
}
