
import scala.jdk.CollectionConverters.*

import io.grpc.*
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder

import cats.effect.*
import fs2.*
import fs2.grpc.syntax.all._

import com.example.protos.hello.*
import com.example.grpc.health.healthCheck.*

class HealthFs2GrpcImpl extends HealthFs2Grpc[IO, Metadata]:

  override def check(request: HealthCheckRequest, ctx: Metadata): IO[HealthCheckResponse] =
    if request.service == "healthservice" then
      IO(HealthCheckResponse.of(HealthCheckResponse.ServingStatus.SERVING))
    else
      IO(HealthCheckResponse.of(HealthCheckResponse.ServingStatus.SERVICE_UNKNOWN))

  override def watch(request: HealthCheckRequest, ctx: Metadata): Stream[IO, HealthCheckResponse] =
    Stream[IO, HealthCheckResponse](HealthCheckResponse.of(HealthCheckResponse.ServingStatus.SERVING))

class GreeterFs2GrpcImpl extends GreeterFs2Grpc[IO, Metadata]:
  override def sayHello(request: HelloRequest, ctx: Metadata): IO[HelloReply] =
    IO(HelloReply.of(s"Request name is: ${request.name}"))

  override def sayHelloStream(request: Stream[IO, HelloRequest], ctx: Metadata): Stream[IO, HelloReply] =
    for
      name <- request.map(_.name)
      result <- Stream[IO, HelloReply](HelloReply.of(s"Request name is: $name"))
    yield result

object Main extends IOApp:

  private val helloService: Resource[IO, ServerServiceDefinition] =
    GreeterFs2Grpc.bindServiceResource[IO](new GreeterFs2GrpcImpl())

  private val healthService: Resource[IO, ServerServiceDefinition] =
    HealthFs2Grpc.bindServiceResource[IO](new HealthFs2GrpcImpl())

  private def runService(services: ServerServiceDefinition*): IO[Nothing] = NettyServerBuilder
    .forPort(9999)
    .addServices(services.asJava)
    .resource[IO]
    .evalMap(server => IO(server.start()))
    .useForever

  override def run(args: List[String]): IO[ExitCode] =
    (for
      v1 <- helloService
      v2 <- healthService
    yield List(v1, v2)).use(runService).as(ExitCode.Success)
