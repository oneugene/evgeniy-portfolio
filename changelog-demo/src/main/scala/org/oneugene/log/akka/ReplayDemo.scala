package org.oneugene.log.akka

import java.time.Month

import akka.actor.{ActorRef, ActorSystem}
import akka.routing.BroadcastGroup
import org.oneugene.log.model.{BDate, User}

/**
  * A simple program to show collection and replay of the changelog
  */
object ReplayDemo extends App {
  val system: ActorSystem = ActorSystem("changelogApplication")

  val initialUser = User("Ievgenii Onyshchenko", BDate(1978, Month.OCTOBER, 3))

  try {
    val loggerActor: ActorRef = system.actorOf(ChangelogPrintActor.props, "printerActor")
    val userMirrorActor: ActorRef = system.actorOf(UserMirrorActor.props(initialUser), "mirrorActor")

    val paths = List(userMirrorActor.path.toString, loggerActor.path.toString)
    val changesBroadcastActor = system.actorOf(BroadcastGroup(paths).props, "changesBroadcast")
    val userActor: ActorRef = system.actorOf(UserActor.props(changesBroadcastActor, initialUser), "userActor")
    val userServiceActor: ActorRef = system.actorOf(UserServiceActor.props(userActor), "userServiceActor")

    userServiceActor ! "start"
  } finally {
    Thread.sleep(1000L)
    system.terminate()
  }
}
