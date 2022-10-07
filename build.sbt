name := """CovidDataset"""
organization := "fr.mind7"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.17"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "3.11.3"
// Swagger-ui
libraryDependencies ++= Seq(
  "com.github.dwickern" %% "swagger-play2.8" % "3.1.0",
  "io.swagger" % "swagger-core" % "1.6.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.1"
)
// Elastic
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "8.4.2"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "fr.mind7.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "fr.mind7.binders._"
