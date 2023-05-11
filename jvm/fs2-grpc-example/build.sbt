ThisBuild / version      := "1.0.0"
ThisBuild / organization := "io.github.takapi327"
ThisBuild / scalaVersion := "3.2.2"
ThisBuild / startYear    := Some(2023)

lazy val protobuf = (project in file("protobuf"))
    .settings(name := "protobuf")
    .enablePlugins(Fs2Grpc)

lazy val client = (project in file("client"))
    .settings(name := "client")
    .dependsOn(protobuf)

lazy val server = (project in file("server"))
    .settings(name := "server")
    .dependsOn(protobuf)
