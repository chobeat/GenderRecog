package genderpredictor

import akka.actor.{ActorRef, TypedProps, Props, TypedActor}
import akka.util.Timeout
import org.apache.spark.PredictorActor.Prediction

/**
 * Created by simone on 14/05/15.
 */
trait OuterAccessActor {
   def predict(x:String)
}

class OuterAccessActorImpl(predictor:ActorRef) extends OuterAccessActor{
  val context=TypedActor.context




  def predict(name:String)={
    println("name")
    predictor ! name
  }


}

object OuterAccessActor{
  def props(predictor: ActorRef)=TypedProps(classOf[OuterAccessActor],new OuterAccessActorImpl(predictor))
}