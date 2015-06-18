import _root_.sbt.Keys._

name := """GenderRecog"""

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "bintray" at "http://dl.bintray.com/shinsuke-abe/maven"

libraryDependencies += "com.github.Shinsuke-Abe" %% "twitter4s" % "2.1.0"
// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.2.0"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.2.1"

libraryDependencies += "org.apache.spark" % "spark-sql_2.10" % "1.2.1"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.2.1"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.2.1"

libraryDependencies += "org.apache.spark" % "spark-graphx_2.10" % "1.2.1"

libraryDependencies += "org.apache.spark" % "spark-hive_2.10" % "1.2.1"

libraryDependencies += "org.apache.spark" % "spark-streaming-twitter_2.10" % "1.2.1"

libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "3.0.3"

libraryDependencies += "com.google.code.gson" % "gson" % "2.2.4"

libraryDependencies += "com.lowagie" % "itext" % "4.2.1"

libraryDependencies += "com.assembla.scala-incubator" %% "graph-core" % "1.9.2"

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.2.0-RC6"
)

incOptions := incOptions.value.withNameHashing(false)