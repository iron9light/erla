name := "erla"

organization := "iron9light.util"

version := "0.2-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "org.scala-lang.modules" %% "scala-async" % "0.9.2",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)