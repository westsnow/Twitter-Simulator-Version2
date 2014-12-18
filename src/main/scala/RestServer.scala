/**
 * created by shuai wu at 15th,Dec,2014
 * This actor is invisible. It's used to connect central server(with scala message)
 *  and RestApi Server.
 */

package main.scala

import akka.actor.ActorSelection
import akka.actor._
import spray.routing.{HttpService, RequestContext}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.collection.mutable.ArrayBuffer


class RestServer(centralServer: ActorSelection) extends Actor {
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val innersystem = ActorSystem()

  import innersystem.dispatcher

  def receive = {
    case s: String =>
      println("receive a string: " + s)

    // get request
    case GetUserHomeline(userId: Int, num: Int) =>
      val future = centralServer ? TwitterRequest(userId, num)
      sender ! future
//      centralServer ! TwitterRequest(userId, num)
    case GetFollowersIds(userId:Int) =>
      val future = centralServer ? GetFollowers(userId)
      sender ! future
      //centralServer ! GetFollowers(userId)
    // post request
    case StatusUpdate(userId: Int, content: String) =>
      centralServer ! SendTwitter(userId, content)
    case StatusDestroy(userId, twitterId) =>
      centralServer ! TwitterDelete(userId, twitterId)
    case FriendShipCreate(userId:Int, followingId:Int)=>
      centralServer ! AddFriend(userId, followingId)
  }
}
