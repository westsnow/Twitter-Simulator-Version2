package main.scala

import akka.actor.ActorRef
import scala.collection.mutable.ArrayBuffer


//rest server to central server
case object RestServerRegister
case class FollowerList(userId:Int, friendList: ArrayBuffer[Int])
case class SendTwitter(userId:Int, content:String)
case class TwitterRequest(userId:Int, num:Int)
case class GetFollowers(userId:Int)
case class TwitterDelete(userId:Int, twitterId:Int)
case class AddFriend(userId:Int, followingId:Int)

//server to client
case object ShutDown
case class AssignUserId(userId:Int, totalUserNum:Int)
case class TwitterResponse(twitters:ArrayBuffer[String])

//client to central server
case object Register

//RestServerMain to RestServer
case class GetUserHomeline(userId:Int, num:Int)
case class StatusUpdate(userId:Int, content:String)
case class GetFollowersIds(userId:Int)
case class StatusDestroy(userId:Int, twitterId:Int)
case class FriendShipCreate(userId:Int, followingId:Int)

