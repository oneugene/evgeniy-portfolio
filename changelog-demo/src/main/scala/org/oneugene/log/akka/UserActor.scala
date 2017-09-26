package org.oneugene.log.akka

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import org.oneugene.log.PropertyChange
import org.oneugene.log.model.User
import org.oneugene.log.play.{NoChangesRecord, PropertyChangeRecord}

import scala.collection.mutable.ArrayBuffer

/**
  * Contains user object and allows to modify user
  *
  * @param changeNotifications listeners to receive changes events
  * @param user                initial state
  */
class UserActor(val changeNotifications: ActorRef, var user: User) extends Actor {
  val log: LoggingAdapter = Logging(context.system, this)
  val changelog: ArrayBuffer[PropertyChange[_]] = ArrayBuffer.empty[PropertyChange[_]]

  override def receive: PartialFunction[Any, Unit] = {
    case PropertyChangeRecord(changedValue, change) if changedValue.isInstanceOf[User] =>
      user = changedValue.asInstanceOf[User]
      changelog += change
      changeNotifications ! change
      log.info(s"Changed user to $user")
    case NoChangesRecord =>
    case UserActor.GetUser =>
      sender ! user
  }
}

object UserActor {
  def props(changeNotifications: ActorRef, user: User): Props =
    Props(new UserActor(changeNotifications, user))

  case class GetUser()

}
