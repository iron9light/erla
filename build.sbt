name := "erla"

organization := "iron9light.util"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test"
)

autoCompilerPlugins := true

libraryDependencies +=
  compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.2")

scalacOptions += "-P:continuations:enable"