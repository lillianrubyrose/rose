package cc.sapphiretech.rose.routes

import cc.sapphiretech.rose.ext.authDelete
import cc.sapphiretech.rose.ext.authPut
import cc.sapphiretech.rose.ext.authedUser
import cc.sapphiretech.rose.models.BasicWebResponse
import cc.sapphiretech.rose.models.RosePermission
import cc.sapphiretech.rose.services.RoleService
import cc.sapphiretech.rose.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class UserRolePut(val targetUser: Int, val roleId: Int)

@Serializable
data class UserRoleDelete(val targetUser: Int, val roleId: Int)

@Suppress("DuplicatedCode")
fun Routing.configureUserRoutes() {
    val userService by inject<UserService>()
    val roleService by inject<RoleService>()

    authPut<UserRolePut>("/user/role", arrayOf(RosePermission.MANAGE_USERS)) { body ->
        val target = userService.findById(body.targetUser)
        if (target == null) {
            call.respond(HttpStatusCode.NotFound, BasicWebResponse("USER"))
            return@authPut
        }

        val authedUser = call.authedUser()
        if(authedUser.compareTo(target) != 1 && !authedUser.superuser) {
            call.respond(HttpStatusCode.Forbidden)
            return@authPut
        }

        val role = roleService.findById(body.roleId)
        if (role == null) {
            call.respond(HttpStatusCode.NotFound, BasicWebResponse("ROLE"))
            return@authPut
        }

        userService.addRole(target, role)
        call.respond(HttpStatusCode.OK)
    }

    authDelete<UserRoleDelete>("/user/role", arrayOf(RosePermission.MANAGE_USERS)) { body ->
        val target = userService.findById(body.targetUser)
        if (target == null) {
            call.respond(HttpStatusCode.NotFound, BasicWebResponse("USER"))
            return@authDelete
        }

        val authedUser = call.authedUser()
        if(authedUser.compareTo(target) != 1 && !authedUser.superuser) {
            call.respond(HttpStatusCode.Forbidden)
            return@authDelete
        }

        val role = roleService.findById(body.roleId)
        if (role == null) {
            call.respond(HttpStatusCode.NotFound, BasicWebResponse("ROLE"))
            return@authDelete
        }

        userService.removeRole(target, role)
        call.respond(HttpStatusCode.OK)
    }
}
