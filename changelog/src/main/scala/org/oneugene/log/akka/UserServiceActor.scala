package org.oneugene.log.akka

import akka.actor.{Actor, ActorRef, Props}
import org.oneugene.log.model.{BDateChangeLogLenses, BDateDay, User, UserChangeLogLenses}
import org.oneugene.log.play.ObjectChangeLens._
import org.oneugene.log.play.{NoChangesRecord, ObjectChangeLens, ObjectChangeRecord, PropertyChangeRecord}

import scala.annotation.tailrec

/**
  * Sample actor to run several changes on user in order to show replaying of changelog
  *
  * @param repo reference to user container
  */
class UserServiceActor(val repo: ActorRef) extends Actor {
  val birthDayLens: ObjectChangeLens[User, BDateDay] = (UserChangeLogLenses.birthDateLens ^|-> BDateChangeLogLenses.dayLens).objectChangeLens

  override def receive: PartialFunction[Any, Unit] = {
    case u: User =>
      change(u, 3)
    case _ =>
      repo ! UserActor.GetUser
  }

  @tailrec
  private def change(u: User, day: BDateDay): User = {
    if (day > 30) {
      u
    } else {
      val objectChange: ObjectChangeRecord[User, BDateDay] = birthDayLens.set(day)(u)
      repo ! objectChange
      val newValue = objectChange match {
        case PropertyChangeRecord(changed, _) => changed
        case NoChangesRecord() => u
      }
      change(newValue, day + 1)
    }
  }
}

object UserServiceActor {
  def props(repo: ActorRef): Props = Props(new UserServiceActor(repo))
}
