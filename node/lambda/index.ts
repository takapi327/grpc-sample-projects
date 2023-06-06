
exports.handler = (event: any, context: any, callback: any) => {
  console.log(event)
  console.log(context)
  callback({ message: 'Callback By Lambda' }, 'Call Lambda succeeded')
}
