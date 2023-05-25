import { NestFactory } from '@nestjs/core'
import { GrpcOptions, Transport } from '@nestjs/microservices'
import { AppModule } from './app.module'
import { join } from 'path'

async function bootstrap() {
  const app = await NestFactory.createMicroservice<GrpcOptions>(AppModule, {
    transport: Transport.GRPC,
    options: {
      url:      'localhost:9000',
      package:  ['com.example.protos', 'grpc.health.v1'],
      protoPath: [
        join(__dirname, 'proto/hello.proto'),
        join(__dirname, 'proto/healthcheck.proto')
      ]
    }
  })
  await app.listen()
}
bootstrap()
