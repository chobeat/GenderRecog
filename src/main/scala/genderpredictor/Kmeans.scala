package genderpredictor

/**
 * Created by simone on 18/05/15.
 */


import genderpredictor.Kmeanstest.Cluster
import org.apache.log4j.{Level, Logger}
import org.apache.spark._
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import breeze.linalg._
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.{ChartFrame, ChartFactory, JFreeChart}
import org.jfree.data.xy.DefaultXYDataset
import scalax.chart.api._
import scalax.chart.module._

object Kmeanstest {

  case class Cluster(clusterID: Int, points: Iterable[Vector], centroid: Vector)

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
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
    val plotDataset = new DefaultXYDataset

    //disegno un grafico con gli esempi e i centroidi
    def dataToSeries(data: Iterable[Vector], i: Int) = {
      val (x, y) = data.map(x => (x(0), x(1))).unzip
      plotDataset.addSeries("Serie " + i, Array(x.toArray, y.toArray))

    }
    //una series è un raggruppamento di dati. Usandone più d'una posso plottare gruppi
    //differenti con colori diversi
    val pointToCluster: RDD[(Int, Vector)] = points.map(point => model.predict(point) -> point)

    val centroids = model.clusterCenters
    val clusters: Array[Cluster] = pointToCluster.groupByKey.collect.zip(centroids).map {
      case ((id, points), centroid) => Cluster(id, points, centroid)
    }

    //calcolo il Davies–Bouldin index come metrica di valutazione del cluster
    val dbi = DBI.calculate(clusters)

    println("Valore DBI del cluster = "+dbi)
    clusters.foreach(x => dataToSeries(x.points, x.clusterID))


    val (xc, yc) = model.clusterCenters.map(x => (x(0), x(1))).unzip
    plotDataset.addSeries("Centroidi", Array(xc.toArray, yc.toArray))
    val frame = new ChartFrame(
      "Title",
      ChartFactory.createScatterPlot(
        "Plot",
        "X Label",
        "Y Label",
        plotDataset,
        org.jfree.chart.plot.PlotOrientation.HORIZONTAL,
        false, false, false
      )
    )
    frame.pack()
    frame.setVisible(true)

  }
}

object DBI {

  import math._

  def distance(xs: Vector, ys: Vector) = {
    sqrt((xs.toArray zip ys.toArray).map { case (x, y) => pow(y - x, 2) }.sum)
  }

  def calculate(clusters: Iterable[Cluster]) = {

    val clusterPairs = (for {first <- clusters
                  second <- clusters if first != second}
      yield (first, second)).groupBy(_._1).map(x=>(x._1,x._2.map(_._2)))

    (clusterPairs.map{
      case(first,seconds)=>seconds.map(second=>calculate_cluster_index(first,second)).max
    }).sum/clusters.size
  }

  def calculate_cluster_index(first: Cluster, second: Cluster) =
    calculate_scatter(first) + calculate_scatter(second) / distance(first.centroid, second.centroid)

  def calculate_scatter(c: Cluster): Double =
    c.points.map(point => distance(point, c.centroid)).sum / c.points.size

}
