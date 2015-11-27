package org.jecos.teambuild.league

import akka.actor.{ActorSystem, ActorRef, Props}
import akka.util.Timeout
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import org.jecos.teambuild.league.LeagueActor.{CommandFailure, LeagueCreated, CommandSuccess, CreateLeague}
import org.scalatest.{BeforeAndAfter, WordSpec}
import akka.pattern.ask
import org.scalatest.Matchers._
import scala.concurrent.duration._
import org.scalatest.Inspectors._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class LeagueActorSpec extends WordSpec with BeforeAndAfter {
  implicit var system: ActorSystem = _
  implicit var timeout: Timeout = _
  var eventLog: ActorRef = _
  var logId: String = _
  var expectedEventActor: ActorRef = _

  var leagueActor: ActorRef = _

  before {
    system = ActorSystem("leagueTest")
    timeout = Timeout(10.seconds)
    logId = System.currentTimeMillis().toString
    eventLog = system.actorOf(LeveldbEventLog.props(logId = "leagueTest" + logId, prefix = "test"))
    expectedEventActor = system.actorOf(Props(new ExpectedEventActor(actorId("ExpectedEvent"), eventLog)))
    leagueActor = system.actorOf(Props(new LeagueActor(actorId("LeagueActor"), eventLog)))
  }

  after {
    system.terminate()
  }

  def actorId(name: String): String = name + logId

  def waitFor(future: Future[Any]): Any = Await.result(future, Duration.Inf)

  def expectedEvents(): List[Any] = {
    val ListEventsSuccess(events) = waitFor(expectedEventActor ? ListEvents())
    events
  }

  "A league actor" when {

    "a league is created" must {

      "return a command success" in {
        val f = leagueActor ? CreateLeague("id")
        val result = waitFor(f)
        result shouldBe CommandSuccess("id")
      }

      "send an appropriate event" in {
        val f = leagueActor ? CreateLeague("id")
        waitFor(f)
        forExactly(1, expectedEvents) { case LeagueCreated(leagueId) => leagueId should be("id") }
      }
    }

    "an already existing league is created" must {

      "return a command failure" in {
        val first = leagueActor ? CreateLeague("LeagueActor")
        waitFor(first)

        val leagueActor2 = system.actorOf(Props(new LeagueActor(actorId("LeagueActor2"), eventLog)))
        val second = leagueActor2 ? CreateLeague("LeagueActor2")
        waitFor(second)


        val leagueActor3 = system.actorOf(Props(new LeagueActor(actorId("LeagueActor"), eventLog)))
        val third = leagueActor3 ? CreateLeague("LeagueActor")
        waitFor(third)

        val result2 = waitFor(second)
        result2 shouldBe CommandSuccess("LeagueActor2")

        val result = waitFor(third)
        result shouldBe a [CommandFailure]
        result.asInstanceOf[CommandFailure].cause shouldBe a [LeagueAlreadyExistException]
      }


    }
  }
}
