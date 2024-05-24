package cc.sapphiretech.rose

import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val config = Config.fromEnv()
    embeddedServer(Netty, port = config.port, host = config.bindIp) {
        configureApp(config)
    }.start(wait = true)
}
