
import io.grpc.*
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder

import com.comcast.ip4s.*

import cats.effect.*

import fs2.grpc.syntax.all.*

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.server.{ Router, ServerRequestKeys }
import org.http4s.implicits.*
import org.http4s.ember.server.EmberServerBuilder

import com.example.protos.hello.*

val managedChannelResource: Resource[IO, ManagedChannel] =
  NettyChannelBuilder
    .forAddress("product-a-service-0efefbf6dc44aa7b4.7d67968.vpc-lattice-svcs.ap-northeast-1.on.aws", 9000)
    .usePlaintext()
    .resource[IO]

def runProgram(stub: GreeterFs2Grpc[IO, Metadata]): IO[Unit] =
  stub.sayHello(HelloRequest.of("takapi"), new Metadata()).flatMap(v => IO.println(v.message))

object Main extends ResourceApp.Forever:

  override def run(args: List[String]): Resource[IO, Unit] =
    for
      channel <- managedChannelResource
      client <- GreeterFs2Grpc.stubResource[IO](channel)
      _ <- EmberServerBuilder.default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"9000")
        .withHttpApp(HttpRoutes.of[IO] {
          case GET -> Root / "healthcheck" => Ok("Healthcheck Ok")
          case GET -> Root =>
            for
              response <- client.sayHello(HelloRequest.of("takapi"), new Metadata())
              result <- Ok(response.message)
            yield result
        }.orNotFound)
        .build
    yield ()
