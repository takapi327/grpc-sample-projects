
exports.handler = async (event: any, context: any, callback: any) => {
  console.log(event)
  console.log(context)
  return {
    statusCode: 200,
    body: 'Callback By Lambda'
  }
}
