/**
 * created by shuai wu at 15th,Dec,2014
 *
 * CentralServerControler
 * starts a central server, which communicates with RestApi Server.
 */

package main.scala

import akka.actor._
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

case object StopWork


object CentralServerMain {


  def main(args: Array[String]): Unit = {

    var sec = 600
    var clientNum = 20

    if (args.length < 1) {
      println("Error:parameter not enough")
    } else {
      clientNum = args(0).toInt
    }


    val serverSystem = ActorSystem("serverSys", ConfigFactory.load(ConfigFactory.parseString( """
  akka {
    actor {
      provider = "akka.remote.RemoteActorRefProvider"
    }
    remote {
      enabled-transports = ["akka.remote.netty.tcp"]
      transport = "akka.remote.netty.NettyRemoteTransport"
      netty.tcp {
        port = 11111
      }
    }
    log-dead-letters = off
  } """)))

    val server = serverSystem.actorOf(Props(classOf[CentralServer], sec, clientNum), name = "server")

    import serverSystem.dispatcher
    serverSystem.scheduler.scheduleOnce(Duration(sec, TimeUnit.SECONDS), server, StopWork)

  }
}

//(Props(classOf[ChatClientActor], server, identity)