package org.oneugene.log.akka

import akka.actor.{Actor, Props}
import akka.event.{Logging, LoggingAdapter}
import org.oneugene.log.PropertyChange
import org.oneugene.log.model.{User, UserPropertyChangeReplay}
import org.oneugene.log.play.NoChangesRecord

/**
  * Slave service for user, it listens for change notifications and replays changes on local copy of user
  *
  * @param user initial state
  */
class UserMirrorActor(var user: User) extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)

  override def receive = {
    case p: PropertyChange[_] =>
      UserPropertyChangeReplay.replayChange(user, p) match {
        case Right(u) => user = u
          log.info(s"Changed user to $user")
      }
    case NoChangesRecord =>
  }

  override def postStop(): Unit = {
    log.info("Stopped")
  }
}

object UserMirrorActor {
  def props(user: User): Props = Props(new UserMirrorActor(user))
}
