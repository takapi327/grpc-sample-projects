import { PUBLIC_API_HOST, PUBLIC_API_PORT } from '$env/static/public'

import { credentials } from '@grpc/grpc-js'
import { GreeterClient, HelloRequest, HelloReply } from '../lib/generated/protobuf/hello'

export const load = async () => {
  const client = new GreeterClient(
    `${ PUBLIC_API_HOST }:${ PUBLIC_API_PORT }`,
    credentials.createInsecure()
  )

  const request: HelloRequest = { name: 'takapi' }

  const response = new Promise<HelloReply>((resolve, reject) => {
    client.sayHello(request, async (error, response) => {
      if(error && error.code !== 0) return reject(error)
      return resolve(response)
    })
  })

  const helloReply: HelloReply = await response
  return {
    message: helloReply.message
  }
}
