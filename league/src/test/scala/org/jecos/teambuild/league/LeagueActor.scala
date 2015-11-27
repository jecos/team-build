package org.jecos.teambuild.league

import akka.actor.{ActorLogging, ActorRef}
import com.rbmhtechnology.eventuate.EventsourcedActor
import org.jecos.teambuild.league.LeagueActor._

import scala.util.{Failure, Success}

object LeagueActor {

  case class CreateLeague(val leagueId: String)

  case class LeagueCreated(val leagueId: String)

  // General replies
  case class CommandSuccess(orderId: String)

  case class CommandFailure(orderId: String, cause: Throwable)

  case class League(val leagueId: String)

}


class LeagueActor(val leagueId: String, override val eventLog: ActorRef) extends EventsourcedActor with ActorLogging {

  override val id: String = s"league-$leagueId"
  override val aggregateId = Some(leagueId)

  private var league: Option[League] = None

  override val onCommand: Receive = {

    case c: CreateLeague => {

      league match {
        case None => {
          persist(LeagueCreated(c.leagueId)) {
            case Success(evt) => {
              onEvent(evt)
              sender() ! CommandSuccess(evt.leagueId)
            }
            case Failure(cause) => sender() ! CommandFailure(c.leagueId, cause)
          }
        }
        case Some(_) => sender() ! CommandFailure(c.leagueId, new LeagueAlreadyExistException())
      }
    }
  }

  override def onEvent: Receive = {
    case LeagueCreated(id) => league = {
      log.info(s"I am $leagueId,and I Receive LeagueCreated($id)")
      Some(League(id))
    }
  }

}


