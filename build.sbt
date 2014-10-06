name := "erla"

organization := "iron9light.util"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test"
)

autoCompilerPlugins := true

libraryDependencies +=
  compilerPlugin("org.scala-lang.plugins" % "continuations" % scalaVersion.value)

scalacOptions ++= Seq("-P:continuations:enable", "–optimise", "-target:jvm-1.7")