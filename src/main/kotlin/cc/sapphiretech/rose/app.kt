package cc.sapphiretech.rose

import cc.sapphiretech.rose.plugins.*
import io.ktor.server.application.*

fun Application.configureApp(config: Config) {
    val dbModule = configureDatabase(config)
    configureKoin(config, dbModule)
    configureMisc()
    configureCors()
    configureAuthentication()
    configureRouting()
}
