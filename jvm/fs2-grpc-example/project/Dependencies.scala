import sbt.*

object Dependencies {

  val grpcNetty    = "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion
  val grpcServices = "io.grpc" % "grpc-services" % "1.53.0"

  val logback = "ch.qos.logback" % "logback-classic" % "1.4.6"

  val http4s: Seq[ModuleID] = Seq(
    "http4s-dsl",
    "http4s-ember-server",
    "http4s-ember-client",
    "http4s-circe"
  ).map("org.http4s" %% _ % "0.23.18")

  val typesafeConfig = "com.typesafe" % "config" % "1.4.2"
  
  val circe = "io.circe" %% "circe-generic" % "0.14.5"
}
