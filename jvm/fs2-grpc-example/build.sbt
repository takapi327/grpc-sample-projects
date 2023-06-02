import com.amazonaws.regions.{ Region, Regions }

import ReleaseTransformations.*
import com.typesafe.sbt.packager.docker.*

import Dependencies.*

ThisBuild / organization := "io.github.takapi327"
ThisBuild / scalaVersion := "3.3.0"
ThisBuild / startYear    := Some(2023)

lazy val commonSettings = Seq(
  run / fork := true,

  javaOptions ++= Seq(
    "-Dconfig.file=conf/env.dev/application.conf",
    "-Dlogback.configurationFile=conf/env.dev/logback.xml"
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
    typesafeConfig
  ) ++ http4s)
  .settings(
    Compile / resourceDirectory := baseDirectory(_ / "conf").value,
    Universal / mappings ++= Seq(
      ((Compile / resourceDirectory).value / "env.stg/application.conf") -> "conf/env.stg/application.conf",
      ((Compile / resourceDirectory).value / "env.stg/logback.xml") -> "conf/env.stg/logback.xml"
    ),

    Docker / maintainer := "takahiko.tominaga+aws_takapi327_product_b@nextbeat.net",
    dockerBaseImage := "amazoncorretto:11",
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

lazy val jmxExporterJavaAgent = {

  val port = 9090
  val conf = "conf/jmx_exporter_conf.yaml"

  val arguments = s"$port:$conf"

  val agent = JavaAgent(
    "io.prometheus.jmx" % "jmx_prometheus_javaagent" % "0.18.0" % "compile;test",
    arguments = arguments
  )

  println(s"jmx_exporter args: '${arguments}'")
  println(s"Adding JavaAgent: ${agent}")
  println(s"JavaAgent.arguments = '${agent.arguments}'")
  println(s"jmx exporter metrics should be available at http://localhost:${port}/metrics")

  agent
}

lazy val server = (project in file("server"))
  .settings(name := "server")
  .settings(libraryDependencies ++= List(
    grpcNetty,
    grpcServices
  ))
  .settings(
    Compile / resourceDirectory := baseDirectory(_ / "conf").value,
    Universal / mappings ++= Seq(
      ((Compile / resourceDirectory).value / "jmx_exporter_conf.yaml") -> "conf/jmx_exporter_conf.yaml"
    ),

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
    },

    javaAgents ++= Seq(jmxExporterJavaAgent)
  )
  .dependsOn(protobuf)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .enablePlugins(EcrPlugin)
  .enablePlugins(JavaAgent)

lazy val root = (project in file("."))
  .settings(publish / skip := true)
  .aggregate(protobuf, client, server)
