package cc.sapphiretech.rose.plugins

import cc.sapphiretech.rose.services.JWTService
import cc.sapphiretech.rose.services.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureAuthentication() {
    val jwtService by inject<JWTService>()
    val userService by inject<UserService>()

    authentication {
        jwt {
            realm = jwtService.realm
            verifier(jwtService.verifier)

            validate { credential ->
                userService.findById(credential.subject!!.toInt())
            }
        }
    }
}