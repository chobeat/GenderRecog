package genderpredictor

/**
 * Created by simone on 18/05/15.
 */

import org.apache.spark._
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.mllib.linalg.distributed._
import org.apache.spark.rdd.RDD
import breeze.linalg._
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.{ChartFrame, ChartFactory, JFreeChart}
import org.jfree.data.xy.DefaultXYDataset
import scalax.chart.api._
import scalax.chart.module._

object Kmeanstest {
  def main(args: Array[String]): Unit = {
    //inizializzazione di Spark
    val conf = new SparkConf().setMaster("local[*]").setAppName("Esempio KMeans")
    val sc = new SparkContext(conf)

    //leggo e faccio parsing di un file di esempio
    val data = sc.textFile("mouse.csv")
      .map(x => x.split(" ").take(2)).filter(x => !x(0).contains("#")).map(_.map(_.toDouble))

    //mappo i dati su un Vector di MLlib
    val points: RDD[Vector] = data.map(x => Vectors.dense(x))

    //addestro Kmeans sugli esempi
    val numClusters = 3
    val numIterations = 20
    val model = KMeans.train(points, numClusters, numIterations)
    val dataset = new DefaultXYDataset

    //disegno un grafico con gli esempi e i centroidi
    def dataToSeries(data: RDD[Vector],i:Int) ={
      val (x, y) = data.collect.map(x => (x(0), x(1))).unzip
      dataset.addSeries("Serie "+i, Array(x.toArray, y.toArray))

    }
    //una series è un raggruppamento di dati. Usandone più d'una posso plottare gruppi
    //differenti con colori diversi
    val clusteredData = points.map(point => (point, model.predict(point)))
    val clusters = (0 to numClusters-1).map(n=>(clusteredData.filter(p => p._2 == n).map(x=>x._1),n))

    clusters.foreach(x=>dataToSeries(x._1,x._2))

    val (xc, yc) = model.clusterCenters.map(x => (x(0), x(1))).unzip
    dataset.addSeries("Centroidi", Array(xc.toArray, yc.toArray))
    val frame = new ChartFrame(
      "Title",
      ChartFactory.createScatterPlot(
        "Plot",
        "X Label",
        "Y Label",
        dataset,
        org.jfree.chart.plot.PlotOrientation.HORIZONTAL,
        false, false, false
      )
    )
    frame.pack()
    frame.setVisible(true)

  }
}
