import sbt.*

object Dependencies {

  val grpcNetty    = "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion
  val grpcServices = "io.grpc" % "grpc-services" % "1.53.0"

  val logback = "ch.qos.logback" % "logback-classic" % "1.4.6"

  val http4s: Seq[ModuleID] = Seq(
    "http4s-dsl",
    "http4s-ember-server"
  ).map("org.http4s" %% _ % "0.23.18")
}
