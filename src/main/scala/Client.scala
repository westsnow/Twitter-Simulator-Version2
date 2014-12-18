/**
 * created by shuai wu at 15th,Dec,2014
 *
 * Client .
 * simulate the behavior of twitter user, including posting and receiving twitters.
 */

package main.scala

import akka.actor._
import org.json4s.native.JsonMethods._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask
import akka.io.IO
import scala.util.Random
import spray.http.Uri
import spray.http._
import HttpMethods._
import spray.client.pipelining._

case object GenerateTwitter
case object GetTwitter


class Client(restIP: String,restPort:Int, centralServer: ActorSelection, followerNum:Int, twitterRequestedNum:Int,
             postTwitterInterval:FiniteDuration, flushTwitterInterval:FiniteDuration) extends Actor {




  implicit val innersystem = ActorSystem()
  import innersystem.dispatcher
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  var userId:Int = -1;
  val randomGenerator:Random = new Random
  var postTwitterTimer:Cancellable = null
  var flushTwitterTimer:Cancellable = null
  val preURI = s"http://$restIP:" + restPort


  centralServer ! Register

//  sendTwitter("ping2")
//  getInfo("hello")
//  getTwitter

  def printResponse(result: Future[HttpResponse]) = {
    result.foreach { response =>
      println(s"Request completed with status ${response.status} and content:\n${response.entity.asString}")
    }
  }



  def sendTwitter(content:String) = {
    val pipeline2 = sendReceive
    val uri = Uri(preURI + "/statuses/update")
    val result = pipeline2(Post(uri.withQuery("userId" -> userId.toString, "content" -> content)))
//      pipeline2(Post(uri.withQuery("param2" -> "2", "param3" -> "i love you", "p4" -> "$%^&* i do")))
    //    val result = pipeline2(Post(s"http://localhost:8080/statuses/update?userId=$userId&content=$content"))
    //printResponse(result)
  }
  def getTwitter = {
    val result = pipeline(Get(preURI + "/" + s"statuses/user_timeline?userId=$userId&num=$twitterRequestedNum"))
    //printResponse(result)
  }
  def getInfo(content:String) = {
    val result = pipeline(Get(preURI + "/" + content))
    //printResponse(result)
  }

  def simulateBegin = {
    postTwitterRepeatly
    flushTwitterRepeatly
  }

  def postTwitterRepeatly = {
    val system = context.system
    import system.dispatcher
    val timeToStart = randomGenerator.nextInt(1000)
    postTwitterTimer = context.system.scheduler.schedule(timeToStart.milliseconds, postTwitterInterval, self, GenerateTwitter)
  }

  def flushTwitterRepeatly = {
    val system = context.system
    import system.dispatcher
    val timeToStart = randomGenerator.nextInt(1000)
    flushTwitterTimer = context.system.scheduler.schedule(timeToStart.milliseconds, flushTwitterInterval, self, GetTwitter)
  }

  def receive={
    case ShutDown=>
      println("server is down, twitter done")
      context stop self
    case s:String=>
    //      println("from server: " + s)
    case AssignUserId(id, totalUserNum)=>
      userId = id
      val follower = generateFollower(totalUserNum)
      //println("user id is " + userId)
      centralServer ! FollowerList(userId, follower)
      simulateBegin
    case GenerateTwitter=>
      val content:String = "this is a twitter from " + userId + "!"
      //      println(userId + "have sent a twitter")
      //      server ! SendTwitter(userId, content)
      sendTwitter(content)
    case GetTwitter=>
      getTwitter
    case TwitterResponse(twitters:ArrayBuffer[String])=>
    //      receiveTwitters(twitters)
  }

  def receiveTwitters(twitters:ArrayBuffer[String]) = {
    println(userId + "'s home line")
    if( twitters.isEmpty )
      println("no new twitter available")
    else
      println("new twitters:")
    for(t <- twitters)
      println(t)
  }
  def generateFollower(totalUserNum:Int): ArrayBuffer[Int]= {
    var follower = ArrayBuffer[Int]()
    for(i <- 1 to followerNum){
      follower += randomGenerator.nextInt(totalUserNum)
    }
    //    println("i am client" + userId + "---my follower is " + follower)
    return follower
  }
}