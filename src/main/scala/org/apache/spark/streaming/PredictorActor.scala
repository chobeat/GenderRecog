/**
 * Created by simone on 14/05/15.
 */
package org.apache.spark

import akka.actor._
import org.apache.spark.streaming.receiver.ActorHelper


class PredictorActor extends Actor with ActorHelper{

  override def preStart()={
    println("Starting...")
  }

  def receive = {
     case s:String => store(s)
  }


}

object PredictorActor{
  case class Prediction(s:String)

  def props=Props(new PredictorActor)
}
