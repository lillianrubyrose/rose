package cc.sapphiretech.rose.routes

import cc.sapphiretech.rose.ext.authPost
import cc.sapphiretech.rose.generated.toWebError
import cc.sapphiretech.rose.models.RosePermission
import cc.sapphiretech.rose.services.RoleService
import com.github.michaelbull.result.getOrElse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class RoleCreate(val name: String)

fun Routing.configureRoleRoutes() {
    postRoleCreate()
}

fun Routing.postRoleCreate() {
    val roleService by inject<RoleService>()

    authPost<RoleCreate>("/role", arrayOf(RosePermission.MANAGE_ROLES)) { body ->
        val role = roleService.create(body.name).getOrElse { err ->
            call.respond(HttpStatusCode.UnprocessableEntity, err.toWebError())
            return@authPost
        }

        call.respond(HttpStatusCode.Created, role.toDTO())
    }
}