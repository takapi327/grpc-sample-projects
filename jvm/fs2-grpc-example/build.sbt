import com.amazonaws.regions.{ Region, Regions }

import ReleaseTransformations.*
import com.typesafe.sbt.packager.docker.*

import Dependencies.*

ThisBuild / organization := "io.github.takapi327"
ThisBuild / scalaVersion := "3.2.2"
ThisBuild / startYear    := Some(2023)

lazy val commonSettings = Seq(
  run / fork := true,

  javaOptions ++= Seq(
    "-Dconfig.file=conf/application.conf",
    "-Dlogback.configurationFile=conf/logback.xml"
  ),

  scalacOptions ++= Seq(
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions"
  )
)

lazy val protobuf = (project in file("protobuf"))
  .settings(name := "protobuf")
  .enablePlugins(Fs2Grpc)

lazy val client = (project in file("client"))
  .settings(name := "client")
  .settings(commonSettings)
  .settings(libraryDependencies ++= List(
    grpcNetty,
    logback,
  ) ++ http4s)
  .settings(
    Compile / resourceDirectory := baseDirectory(_ / "conf").value,
    Universal / mappings ++= Seq(
      ((Compile / resourceDirectory).value / "application.conf") -> "conf/application.conf",
      ((Compile / resourceDirectory).value / "logback.xml") -> "conf/logback.xml"
    ),

    Docker / maintainer := "takahiko.tominaga+aws_takapi327_product_b@nextbeat.net",
    //dockerBaseImage := "amazoncorretto:11",
    dockerBaseImage := "amazonlinux:2023",
    Docker / dockerExposedPorts := Seq(9000, 9000),
    Docker / daemonUser := "daemon",
    dockerCommands := {
      dockerCommands.value.flatMap {
        case down@Cmd("USER", "1001:0") => DockerCommands.grpcCurl :+ down
        case other => Seq(other)
      }
    },

    Ecr / region := Region.getRegion(Regions.AP_NORTHEAST_1),
    Ecr / repositoryName := "jvm-microservice-server",
    Ecr / repositoryTags ++= Seq(version.value),
    Ecr / localDockerImage := (Docker / packageName).value + ":" + (Docker / version).value,

    releaseVersionBump := sbtrelease.Version.Bump.Bugfix,

    releaseProcess := {
      Seq[ReleaseStep](
        runClean,
        ReleaseStep(state => Project.extract(state).runTask(Docker / publishLocal, state)._1),
        ReleaseStep(state => Project.extract(state).runTask(Ecr / login, state)._1),
        ReleaseStep(state => Project.extract(state).runTask(Ecr / push, state)._1),
      )
    }
  )
  .dependsOn(protobuf)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(EcrPlugin)

lazy val server = (project in file("server"))
  .settings(name := "server")
  .settings(libraryDependencies ++= List(
    grpcNetty,
    grpcServices
  ))
  .settings(
    Docker / maintainer         := "takahiko.tominaga+aws_takapi327_product_a@nextbeat.net",
    dockerBaseImage             := "amazoncorretto:11",
    Docker / dockerExposedPorts := Seq(9000, 9000),
    Docker / daemonUser         := "daemon",
    dockerCommands := {
      dockerCommands.value.flatMap {
        case down@Cmd("USER", "1001:0") => DockerCommands.grpcHealthProbe :+ down
        case other => Seq(other)
      }
    },

    Ecr / region           :=  Region.getRegion(Regions.AP_NORTHEAST_1),
    Ecr / repositoryName   := "jvm-microservice-server",
    Ecr / repositoryTags   ++= Seq(version.value),
    Ecr / localDockerImage :=  (Docker / packageName).value + ":" + (Docker / version).value,

    releaseVersionBump := sbtrelease.Version.Bump.Bugfix,

    releaseProcess := {
      Seq[ReleaseStep](
        runClean,
        ReleaseStep(state => Project.extract(state).runTask(Docker / publishLocal, state)._1),
        ReleaseStep(state => Project.extract(state).runTask(Ecr / login, state)._1),
        ReleaseStep(state => Project.extract(state).runTask(Ecr / push, state)._1),
      )
    }
  )
  .dependsOn(protobuf)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(EcrPlugin)

lazy val root = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(protobuf, client, server)
