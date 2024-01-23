organization := "io.brainergy"
name := "rest-pdf"
version := "1"
maintainer := "peerapat_a@brainergy.digital"

Compile / doc / sources := Seq.empty
Compile / packageDoc / publishArtifact := false

topLevelDirectory := None
executableScriptName := "start"
Universal / packageName := "api"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayAkkaHttp2Support)

scalaVersion := "2.13.12"
scalacOptions := Seq("-feature", "-deprecation", "-unchecked", "-Ywarn-dead-code")
javacOptions ++= Seq("-source", "11", "-target", "11", "-encoding", "UTF-8")

routesGenerator := InjectedRoutesGenerator

excludeDependencies ++= Seq(
  "com.jolbox" % "bonecp"
  , "commons-codec" % "commons-codec"
  , "org.hibernate.validator" % "hibernate-validator"
  , "org.bouncycastle" % "bcpkix-jdk15on"
  , "org.bouncycastle" % "bcprov-jdk15on"
)

libraryDependencies ++= Seq(
  guice
  , "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2"
  , "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2"
  , "com.itextpdf" % "itext7-core" % "7.2.5"
  , "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
  , "io.minio" % "minio" % "8.5.6"
  , "org.apache.commons" % "commons-lang3" % "3.13.0"
  , "org.bouncycastle" % "bcpkix-jdk18on" % "1.72"
  , "org.bouncycastle" % "bcprov-jdk18on" % "1.72"
  , "org.scalaj" %% "scalaj-http" % "2.4.2"
  , "org.apache.poi" % "poi-ooxml" % "5.2.3"
  , "org.xerial.snappy" % "snappy-java" % "1.1.10.5"
)

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)
