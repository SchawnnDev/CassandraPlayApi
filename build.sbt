name := """CovidDataset"""
organization := "fr.mind7"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.9"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
//libraryDependencies += "com.outworkers" %% "phantom-dsl" % "2.59.0"
//libraryDependencies += "net.codingwell" %% "scala-guice" % "5.1.0"
libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "3.11.3"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "fr.mind7.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "fr.mind7.binders._"
