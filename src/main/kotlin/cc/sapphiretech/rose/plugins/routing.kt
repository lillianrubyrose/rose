package cc.sapphiretech.rose.plugins

import cc.sapphiretech.rose.routes.configureAuthRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        configureAuthRoutes()
    }
}
