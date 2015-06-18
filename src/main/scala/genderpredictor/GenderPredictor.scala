package genderpredictor


import akka.actor.TypedActor
import hyphen._
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkEnv, PredictorActor, SparkConf, SparkContext}

import org.apache.spark.streaming._
import scala.concurrent.Await
import scala.concurrent.duration._
object GenderPredictor {
  type FeatureExtractor= String=>Double
  val genders=List[String]("m","f")
  implicit def bool2Double(b:Boolean) = if (b) 1d else 0d
  val hyphenator=Hyphenator.load("en")
  def extract(name:String)=Vectors.dense((for (extractor<-extractors) yield extractor(name)).toArray)
  def extractSyllablesNum:FeatureExtractor={hyphenator.hyphenate(_).length}
  def extractLength:FeatureExtractor={_.length}
  val isVowel = Set('a', 'e', 'i', 'o', 'u')
  def extractLastIsVowel:FeatureExtractor={x:String=>isVowel(x.last)}
  def extractors:List[FeatureExtractor]=extractLastIsVowel::extractLength::Nil

  def main(args: Array[String]): Unit = {
    //inizializzo spark
    val driverPort = 7777
    val driverHost = "localhost"
    val config= new SparkConf().setMaster("local[2]").setAppName("GenderRecog")
      .set("spark.eventLog.enabled","false").set("spark.driver.port", driverPort.toString)
      .set("spark.driver.host", driverHost)
    val sc= new SparkContext(config)
    //leggo il dataset
    val male=sc.textFile("model_data/male.txt")
    val female=sc.textFile("model_data/female.txt")
    //val dataset=male.map(x=>LabeledPoint(genders.indexOf("m"),extract(x)))++
    //  female.map(x=>LabeledPoint(genders.indexOf("f"),extract(x)))

    val dataset=male.map(x=>LabeledPoint(0D,extract(x)))++
      female.map(x=>LabeledPoint(1D,extract(x)))
    //diido il dataset in training set e test set
    val splits = dataset.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    //creo un modello con SVM
    val numIter=100
    val model=SVMWithSGD.train(training,numIter)

    //faccio associare al modello un etichetta per ogni punto
    val scoreAndLabels = test.map { point =>
      val score = model.predict(point.features)
      (score, point.label)
    }

    //sc.parallelize(Vector(model)).saveAsTextFile("model.dat")

    //valuto la performance del modello usando un utility di Spark
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
    val auROC = metrics.areaUnderROC()
    val error=metrics.areaUnderPR()

    println("Area under ROC = " + auROC)
    println("Area under PR = " + error)




/*
    val ssc= new StreamingContext(sc,Milliseconds(5000))

    val actorStream=ssc.actorStream[String](PredictorActor.props,"PredictorActor")

    actorStream.map(x=>model.predict(extract(x)))
      .foreachRDD(_.foreach(x=>print(x))
    )
      ssc.start()*/
  }
}
