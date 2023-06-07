
import io.grpc.*
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder

import com.comcast.ip4s.*

import cats.effect.*

import com.typesafe.config.*

import fs2.grpc.syntax.all.*

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.ember.client.EmberClientBuilder

import com.example.protos.hello.*

private val config = ConfigFactory.load()

private val host = config.getString("microservice.host")
private val port = config.getInt("microservice.port")
private val isHttps = config.getBoolean("microservice.is_https")

val managedChannelResource: Resource[IO, ManagedChannel] =
  val builder = NettyChannelBuilder.forAddress(host, port)
  (if isHttps then builder
  else builder.usePlaintext()).resource[IO]

def runProgram(stub: GreeterFs2Grpc[IO, Metadata]): IO[Unit] =
  stub.sayHello(HelloRequest.of("takapi"), new Metadata()).flatMap(v => IO.println(v.message))

object Main extends ResourceApp.Forever:

  private val httpHost = config.getString("http.host")
  private val hostPort = config.getInt("http.port")

  override def run(args: List[String]): Resource[IO, Unit] =
    for
      channel <- managedChannelResource
      client <- GreeterFs2Grpc.stubResource[IO](channel)
      restClient <- EmberClientBuilder.default[IO].build
      _ <- EmberServerBuilder.default[IO]
        .withHost(Host.fromString(httpHost).getOrElse(host"0.0.0.0"))
        .withPort(Port.fromInt(hostPort).getOrElse(port"9000"))
        .withHttpApp(HttpRoutes.of[IO] {
          case GET -> Root / "healthcheck" => Ok("Healthcheck Ok")
          case GET -> Root =>
            for
              response <- client.sayHello(HelloRequest.of("takapi"), new Metadata())
              result <- Ok(response.message)
            yield result
          case GET -> Root / "lambda" =>
            for
              response <- restClient.expect[String](uri"http://lambda-service-076004daa2c02b209.7d67968.vpc-lattice-svcs.ap-northeast-1.on.aws:80")
              result <- Ok(response)
            yield result
        }.orNotFound)
        .build
    yield ()
