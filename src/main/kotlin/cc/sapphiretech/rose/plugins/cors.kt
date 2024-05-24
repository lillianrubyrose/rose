package cc.sapphiretech.rose.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() = install(CORS) {
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Patch)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Options)

    allowHeader(HttpHeaders.Authorization)

    // FIXME: Only in dev mode
    anyHost()
}
