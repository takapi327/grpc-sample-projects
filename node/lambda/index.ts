
exports.handler = (event: any, context: any, callback: any) => {
  console.log(event)
  console.log(context)
  callback(null, { message: 'Callback By Lambda' })
}
