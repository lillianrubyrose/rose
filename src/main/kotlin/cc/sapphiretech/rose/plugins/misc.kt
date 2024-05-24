package cc.sapphiretech.rose.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureMisc() {
    install(ContentNegotiation) {
        json()
    }

    install(DefaultHeaders)
}