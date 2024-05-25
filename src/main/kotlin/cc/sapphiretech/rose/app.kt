package cc.sapphiretech.rose

import cc.sapphiretech.rose.plugins.*
import io.ktor.server.application.*

fun Application.configureApp(config: Config) {
    configureKoin(config)
    configureDatabase()
    configureMisc()
    configureCors()
    configureAuthentication()
    configureRouting()
}
