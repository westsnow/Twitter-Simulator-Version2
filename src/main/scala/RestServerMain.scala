/**
 * created by shuai wu at 15th,Dec,2014
 *
 * This is a RestApi Server. It receives the RestApi request from client and transfer the Rest request to actor message.
 * Then forward this message to RestServer.
 * The main logic is in startServer function, which deals with users' requests.
 */

package main.scala
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import spray.routing.SimpleRoutingApp
import spray.http._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._
import spray.http._
import MediaTypes._


object RestServerMain extends App with SimpleRoutingApp {
  implicit val actorSystem = ActorSystem()
  implicit val timeout: Timeout = Timeout(500.milliseconds)

  import actorSystem.dispatcher

  var myport = 8080
  var centralIP = "192.168.1.28"
  if (args.length < 2) {
    println("Error:parameter not enough")
  } else {
    myport = args(0).toInt
    centralIP = args(1)
  }


  def getContent(future: Future[Any]): String = {
    val future2 = Await.result(future, timeout.duration).asInstanceOf[Future[ArrayBuffer[String]]]
    val result = Await.result(future2, timeout.duration).asInstanceOf[ArrayBuffer[String]]

    var str = "{\n"

    for(ss<-result){
      str += "  " + ss + "\n"
    }
    str + "}"
  }

  def getFollowersIds(future: Future[Any]):String = {
    val future2 = Await.result(future, timeout.duration).asInstanceOf[Future[ArrayBuffer[Int]]]
    val result = Await.result(future2, timeout.duration).asInstanceOf[ArrayBuffer[Int]]

    var str = "{\n"

    for(ss<-result){
      str += "  " + ss + "\n"
    }
    str + "}"
  }


  val restSystem = ActorSystem("client", ConfigFactory.load(ConfigFactory.parseString( """
   akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    }
    remote {
      netty.tcp.port = 5555
    }
    log-dead-letters = off
  } """)))

  val centralServerPath = "akka.tcp://serverSys@" + centralIP + ":11111/user/server"
  val centralServer = restSystem.actorSelection(centralServerPath)
  val restServer = restSystem.actorOf(Props(classOf[RestServer], centralServer), name = "restServer")



  startServer(interface = "0.0.0.0", port = myport) {
    get {
      path("index.html") {
        complete {
          index
        }
      }
    } ~
      get {
        path("statuses" / "user_timeline") {
          parameters("userId".as[Int], "num".as[Int]) { (userId, num) =>
            val future = restServer ? GetUserHomeline(userId, num)
            complete {
              getContent(future)
            }
          }
        }
      } ~
    get {
      path("followers" / "ids"){
        parameter("userId".as[Int]) { userId=>
          val future = restServer ? GetFollowersIds(userId)
          complete {
            getFollowersIds(future)
          }
        }
      }
    } ~
      post {
        path("statuses" / "update") {
          parameters("userId".as[Int], "content".as[String]) { (userId, content) =>
            restServer ! StatusUpdate(userId, content)
            complete {
              "twitter updated"
            }
          }
        }
      }~
    post {
      path("statuses" / "destroy" ){
        parameters("userId".as[Int], "twitterId".as[Int]) { (userId, twitterId) =>
          restServer ! StatusDestroy(userId, twitterId)
          complete{
            "twitter deleted"
          }
        }
        }
    } ~
      post {
        path("friendships" / "create") {
          parameters("userId".as[Int], "followingId".as[Int]) { (userId, followingId) =>
            restServer ! FriendShipCreate(userId, followingId)
            complete {
              "friendship created"
            }
          }
        }
      }
  }

  lazy val index = HttpResponse(
    entity = HttpEntity(`text/html`,
      <html>
        <body>
          <h1>Welcome to my <i>twitter world</i></h1>
          <a href="http://westsnow.github.io">RESTAPI can be found here</a>
        </body>
      </html>.toString()
    )
  )
}
