import { Controller } from '@nestjs/common'
import { GrpcMethod, GrpcStreamMethod } from '@nestjs/microservices'

import { Observable, Subject } from 'rxjs'

import { GreeterController, HelloReply, HelloRequest } from './lib/generated/hello'
import {
  HealthCheckRequest,
  HealthCheckResponse,
  HealthCheckResponse_ServingStatus,
  HealthController,
  HealthControllerMethods
} from './lib/generated/healthCheck'

@Controller()
export class AppController implements GreeterController {
  constructor() {}

  @GrpcMethod('Greeter')
  sayHello(request: HelloRequest): HelloReply {
    return {
      message: `Request name is: ${ request.name }`
    }
  }

  @GrpcStreamMethod('Greeter')
  sayHelloStream(request$: Observable<HelloRequest>): Observable<HelloReply> {

    const helloReply$: Subject<HelloReply> = new Subject<HelloReply>()

    request$.subscribe({
      next: (helloRequest: HelloRequest) => helloReply$.next({ message: `Request name is: ${ helloRequest.name }` }),
      complete: () => helloReply$.complete()
    })

    return helloReply$.asObservable()
  }
}

@Controller()
@HealthControllerMethods()
export class HealthCheckController implements HealthController {
  constructor() {}

  check(request: HealthCheckRequest): HealthCheckResponse {
    if (request.service == 'healthservice') {
      return { status: HealthCheckResponse_ServingStatus.SERVING }
    } else {
      return { status: HealthCheckResponse_ServingStatus.SERVICE_UNKNOWN}
    }
  }

  watch(request: HealthCheckRequest): Observable<HealthCheckResponse> {
    const response$: Subject<HealthCheckResponse> = new Subject<HealthCheckResponse>()

    if (request.service == 'healthservice') {
      response$.next({ status: HealthCheckResponse_ServingStatus.SERVING })
    } else {
      response$.next({ status: HealthCheckResponse_ServingStatus.SERVICE_UNKNOWN })
    }

    response$.complete()

    return response$.asObservable()
  }
}
