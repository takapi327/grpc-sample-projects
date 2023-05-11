ThisBuild / version      := "1.0.0"
ThisBuild / organization := "io.github.takapi327"
ThisBuild / scalaVersion := "3.2.2"
ThisBuild / startYear    := Some(2023)

lazy val protobuf = (project in file("protobuf"))
  .settings(name := "protobuf")
  .enablePlugins(Fs2Grpc)

lazy val client = (project in file("client"))
  .settings(name := "client")
  .settings(libraryDependencies ++= List(
    "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion
  ))
  .dependsOn(protobuf)

lazy val server = (project in file("server"))
  .settings(name := "server")
  .settings(libraryDependencies ++= List(
    "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc" % "grpc-services" % "1.53.0"
  ))
  .dependsOn(protobuf)

lazy val root = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(protobuf, client, server)
