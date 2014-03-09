name := "erla"

organization := "iron9light.util"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.4",
  "org.scalatest" %% "scalatest" % "2.1.0" % "test"
)

autoCompilerPlugins := true

libraryDependencies +=
  compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.3")

scalacOptions += "-P:continuations:enable"