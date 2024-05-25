package cc.sapphiretech.rose.models

import io.ktor.server.auth.*

data class RoseUser(
    val id: Int,
    var superuser: Boolean,
    val safeUsername: String,
    val displayName: String,
    val passwordHash: String,
    val roles: MutableSet<RoseRole>
) : Principal {
    fun hasPermission(permission: RosePermission): Boolean = roles.any { it.hasPermission(permission) }
}
