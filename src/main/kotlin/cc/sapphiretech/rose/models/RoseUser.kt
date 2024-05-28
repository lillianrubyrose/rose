package cc.sapphiretech.rose.models

import io.ktor.server.auth.*

data class RoseUser(
    val id: Int,
    var superuser: Boolean,
    val safeUsername: String,
    val displayName: String,
    val passwordHash: String,
    val roles: MutableSet<RoseRole>
) : Principal, Comparable<RoseUser> {
    fun hasPermission(permission: RosePermission): Boolean = roles.any { it.hasPermission(permission) }

    // Compare permission levels.
    // Returns 0  if equal.
    //         1  If greater.
    //         -1 If less.
    //
    // This compares the superuser status of the two users
    // and their highest weighted role.
    override fun compareTo(other: RoseUser): Int {
        if (superuser) {
            if (other.superuser) {
                return 0
            }
            return 1
        }

        if (other.superuser) {
            return -1
        }

        val otherWeight = other.roles.maxOfOrNull { it.weight } ?: 0
        val ourWeight = other.roles.maxOfOrNull { it.weight } ?: 0
        if (ourWeight > otherWeight) {
            return 1
        } else if (otherWeight > ourWeight) {
            return -1
        }

        return 0
    }
}
