import { Module } from '@nestjs/common'
import { AppController, HealthCheckController } from './app.controller'

@Module({
  imports: [],
  controllers: [AppController, HealthCheckController],
  providers: [],
})
export class AppModule {}
