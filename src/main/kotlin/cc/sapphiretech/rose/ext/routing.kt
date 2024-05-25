package cc.sapphiretech.rose.ext

import cc.sapphiretech.rose.models.RosePermission
import cc.sapphiretech.rose.models.RoseUser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

@KtorDsl
inline fun Route.authRoute(
    path: String,
    method: HttpMethod,
    requiredPermissions: Array<RosePermission>? = null,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit,
): Route {
    return route(path, method) {
        authenticate {
            handle {
                if (requiredPermissions != null) {
                    val user = call.principal<RoseUser>()
                        .orThrow { NullPointerException("call.principal is null. Something is very wrong!") }
                    if (user.superuser || requiredPermissions.all { user.hasPermission(it) }) {
                        body()
                    } else {
                        call.respond(HttpStatusCode.Unauthorized)
                    }
                } else {
                    body()
                }
            }
        }
    }
}

@KtorDsl
inline fun Route.authGet(
    path: String,
    requiredPermissions: Array<RosePermission>? = null,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
): Route {
    return authRoute(path, HttpMethod.Get, requiredPermissions, body)
}

@KtorDsl
inline fun <reified R : Any> Route.authGet(
    path: String,
    requiredPermissions: Array<RosePermission>? = null,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(R) -> Unit
): Route {
    return authGet(path, requiredPermissions) {
        body(call.receive())
    }
}

@KtorDsl
inline fun Route.authPost(
    path: String,
    requiredPermissions: Array<RosePermission>? = null,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
): Route {
    return authRoute(path, HttpMethod.Post, requiredPermissions, body)
}

@KtorDsl
inline fun <reified R : Any> Route.authPost(
    path: String,
    requiredPermissions: Array<RosePermission>? = null,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(R) -> Unit
): Route {
    return authPost(path, requiredPermissions) {
        body(call.receive())
    }
}
