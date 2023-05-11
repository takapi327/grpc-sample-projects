
import io.grpc.*
import io.grpc.protobuf.services.ProtoReflectionService
//import io.grpc.netty.NettyServerBuilder
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder

import cats.effect.*
import fs2.*
import fs2.grpc.syntax.all._

import com.example.protos.hello.*

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

  private def runService(service: ServerServiceDefinition) = NettyServerBuilder
    .forPort(9999)
    .addService(service)
    .resource[IO]
    .evalMap(server => IO(server.start()))
    .useForever

  override def run(args: List[String]): IO[ExitCode] =
    helloService.use(runService).as(ExitCode.Success)
