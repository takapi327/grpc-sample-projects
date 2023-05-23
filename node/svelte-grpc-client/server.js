import express from 'express'

import { handler } from './build/handler.js'

const app = express()

// add a route that lives separately from the SvelteKit app
app.get('/healthcheck', (req, res) => {
    res.end('ok')
})

// let SvelteKit handle everything else, including serving prerendered pages and static assets
app.use(handler)
const server = app.listen(9000, () => {})

// Graceful Shutdown
process.on('SIGINT', function () {
    // close connections
    server.close(() => { })
})

process.on('SIGTERM', function () {
    // close connections
    server.close(() => { })
})
