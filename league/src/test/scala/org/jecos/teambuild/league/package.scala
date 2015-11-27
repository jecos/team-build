package org.jecos.teambuild

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.rbmhtechnology.eventuate.EventsourcedView
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import org.scalatest.BeforeAndAfter

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

package object league {

  trait EventuateContext {
    this: BeforeAndAfter =>


  }

  /**
   * list of commands
   */
  case class ListEvents()

  /**
   * replies on command
   */
  case class ListEventsSuccess(events: List[Any])

  /**
   * actor to log all sended events
   * @param id
   * @param eventLog
   */
  class ExpectedEventActor(override val id: String, override val eventLog: ActorRef) extends EventsourcedView {

    var events: List[Any] = List.empty

    override val onCommand: Receive = {
      case ListEvents() => sender() ! ListEventsSuccess(events)
    }

    override val onEvent: Receive = {
      case e => events = events :+ e
    }
  }

}
