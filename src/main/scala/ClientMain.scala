/**
 * created by shuai wu at 15th,Dec,2014
 *
 * Client simulator.
 * starts some simulated twitter users.
 */

package main.scala

import org.json4s._
import scala.util.parsing.json
import akka.actor._
import com.typesafe.config.ConfigFactory
import org.json4s.native.JsonMethods._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import akka.actor._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask
import akka.io.IO
import akka.util.Timeout
import scala.util.Random
import spray.can.Http
import spray.http._
import spray.http.parser.UriParser
import java.net.URLEncoder
import HttpMethods._
import scala.util.parsing.json._

//case object GenerateTwitter
//case object GetTwitter


object ClientMain {

  var actorpool = ArrayBuffer[ActorRef]();
  var clientNum = 10
  var followerNum = 5
  var twitterRequestedNum = 5
  var postTwitterInterval = 10000 milliseconds
  var flushTwitterInterval = 10000 milliseconds


  def main(args: Array[String]): Unit = {

    var centralIP = "192.168.1.28"
    var restIP = "192.168.1.28"
    var restPort = 8080



    if (args.length < 8) {
      println("Error:parameter not enough")
    } else {
      centralIP = args(0)
      restIP = args(1)
      restPort = args(2).toInt
      clientNum = args(3).toInt
      followerNum = args(4).toInt
      twitterRequestedNum = args(5).toInt
      postTwitterInterval = args(6).toInt.milliseconds
      flushTwitterInterval = args(7).toInt.milliseconds
    }

    val clientSystem = ActorSystem("client", ConfigFactory.load(ConfigFactory.parseString( """
   akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    }
    remote {
      netty.tcp.port = 11344
    }
    log-dead-letters = off
  } """)))
    //    implicit val system: ActorSystem = ActorSystem()
    implicit val timeout: Timeout = Timeout(15.seconds)
    //    import system.dispatcher // implicit execution context

    val centralServerPath = "akka.tcp://serverSys@" + centralIP + ":11111/user/server"
    val centralServer = clientSystem.actorSelection(centralServerPath)



    for (i <- 0 until clientNum) {
      val client = clientSystem.actorOf(Props(classOf[Client], restIP,restPort, centralServer, followerNum, twitterRequestedNum,
        postTwitterInterval, flushTwitterInterval), name = "client" + i)
      actorpool += client;
    }

  }


}
