package genderpredictor

import org.apache.spark.{SparkConf, SparkEnv}

import scala.concurrent.Await

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.{ActorSystem, TypedActor}
/**
 * Created by simone on 15/05/15.
 */
object Server{
  def main (args: Array[String]):Unit= {

    val actorSystem = ActorSystem("outerActorSystem")
  val url = "akka.tcp://sparkDriver@localhost:7777/user"
  val timeout = 10 seconds
  val predictor = Await.result(actorSystem.actorSelection(url).resolveOne(timeout), timeout)

  val outer:OuterAccessActor=TypedActor(actorSystem).typedActorOf(OuterAccessActor.props(predictor))
  println(predictor.path)

  outer.predict("anacleto")
  }
}
