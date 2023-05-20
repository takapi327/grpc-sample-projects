import com.amazonaws.regions.{ Region, Regions }

import ReleaseTransformations.*
import com.typesafe.sbt.packager.docker.*

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

lazy val additionalCommands = Seq(
  ExecCmd(
    "RUN",
    "yum",
    "-y",
    "install",
    "wget"
  ),
  ExecCmd(
    "RUN",
    "wget",
    "-q",
    "-O",
    "/bin/grpc_health_probe",
    "https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/v0.3.1/grpc_health_probe-linux-amd64"
  ),
  ExecCmd(
    "RUN",
    "chmod",
    "+x",
    "/bin/grpc_health_probe"
  ),
  ExecCmd(
    "RUN",
    "find ./ -name grpc_health_probe"
  ),
  Cmd(
    "COPY",
    "--from=stage0 --chown=daemon:root ./bin/grpc_health_probe /bin/"
  )
)

lazy val server = (project in file("server"))
  .settings(name := "server")
  .settings(libraryDependencies ++= List(
    "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion,
    "io.grpc" % "grpc-services" % "1.53.0"
  ))
  .settings(
    Docker / maintainer         := "takahiko.tominaga+aws_takapi327_product_a@nextbeat.net",
    dockerBaseImage             := "amazoncorretto:11",
    Docker / dockerExposedPorts := Seq(9000, 9000),
    Docker / daemonUser         := "daemon",
    dockerCommands := {
      dockerCommands.value.flatMap {
        case down@Cmd("USER", "1001:0") => additionalCommands :+ down
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
