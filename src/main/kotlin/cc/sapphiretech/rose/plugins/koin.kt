package cc.sapphiretech.rose.plugins

import cc.sapphiretech.rose.Config
import cc.sapphiretech.rose.services.JWTService
import cc.sapphiretech.rose.services.UserService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin(config: Config) {
    install(Koin) {
        modules(module {
            single { config }
            single { JWTService() }
            single { UserService() }
        })
    }
}