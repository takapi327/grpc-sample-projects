
import io.grpc.*
//import io.grpc.netty.NettyChannelBuilder
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder

import cats.effect.*

import fs2.grpc.syntax.all.*

import com.example.protos.hello.*

val managedChannelResource: Resource[IO, ManagedChannel] =
  NettyChannelBuilder
    .forAddress("127.0.0.1", 9999)
    .usePlaintext()
    .resource[IO]

def runProgram(stub: GreeterFs2Grpc[IO, Metadata]): IO[Unit] =
  stub.sayHello(HelloRequest.of("takapi"), new Metadata()).flatMap(v => IO.println(v.message))

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    managedChannelResource
     .flatMap(ch => GreeterFs2Grpc.stubResource[IO](ch))
     .use(runProgram)
     .as(ExitCode.Success)
