organization := "com.devialab"

name := "sosms-rem"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

val AkkaVersion = "2.3.6"

libraryDependencies ++= Seq(
  "org.clapper" %% "grizzled-slf4j" % "1.0.2",
  "com.devialab" %% "corbel-rem-utils" % "1.0.0-SNAPSHOT",
  "io.corbel" % "rem-api" % "1.24.0-SNAPSHOT" % "provided",
  "io.corbel.lib" % "config" % "0.3.0" % "provided",
  "com.twilio.sdk" % "twilio-java-sdk" % "5.2.0",
  "com.mandrillapp.wrapper.lutung" % "lutung" % "0.0.5",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case x if x.endsWith(".SF") => MergeStrategy.discard
  case x if x.endsWith(".DSA") => MergeStrategy.discard
  case x if x.endsWith(".RSA") => MergeStrategy.discard
  case _ => MergeStrategy.last
}

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

resolvers in ThisBuild ++= Seq(
  Resolver.defaultLocal,
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  "Devialab Repository" at "http://artifacts.devialab.com/artifactory/libs",
  "iQapla repository" at "http://artifacts.devialab.com/artifactory/iqapla"
)

credentials in ThisBuild += Credentials(Path.userHome / ".ivy2" / ".credentials")

// This artifact will be used from java so no need for scala version on artifact name
crossPaths       := false

autoScalaLibrary := false
    