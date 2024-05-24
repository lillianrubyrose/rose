package cc.sapphiretech.rose.routes

import cc.sapphiretech.rose.models.BasicWebResponse
import cc.sapphiretech.rose.services.JWTService
import cc.sapphiretech.rose.services.UserService
import com.github.michaelbull.result.getOrElse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import pm.lily.argon.argonVerify

@Serializable
data class AuthLogin(val username: String, val password: String)

@Serializable
data class AuthRegister(val username: String, val password: String)

fun Routing.configureAuthRoutes() {
    postAuthRegister()
    postAuthLogin()
}

fun Routing.postAuthRegister() {
    val userService by inject<UserService>()
    val jwtService by inject<JWTService>()

    post<AuthRegister>("/auth/register") { body ->
        val user = userService.create(body.username, body.password).getOrElse {
            call.respond(HttpStatusCode.UnprocessableEntity, it.toWebError())
            return@post
        }

        val token = jwtService.createAccessToken(user.id)
        call.respond(BasicWebResponse(token))
    }
}

fun Routing.postAuthLogin() {
    val userService by inject<UserService>()
    val jwtService by inject<JWTService>()

    post<AuthLogin>("/auth/login") { body ->
        val user = userService.findByUsername(body.username).getOrElse {
            call.respond(HttpStatusCode.Unauthorized, "INVALID_LOGIN")
            return@post
        }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized, "INVALID_LOGIN")
            return@post
        }

        if (user.passwordHash.argonVerify(body.password)) {
            val token = jwtService.createAccessToken(user.id)
            call.respond(BasicWebResponse(token))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "INVALID_LOGIN")
        }
    }
}
