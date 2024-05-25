package cc.sapphiretech.rose.models

import kotlinx.serialization.Serializable

data class RoseRole(
    val id: Int,
    val weight: Int,
    val safeName: String,
    val displayName: String,
    val inheritPermissionsFrom: MutableSet<RoseRole>,
    val permissions: MutableCollection<RosePermission>
) {
    fun hasPermission(permission: RosePermission): Boolean =
        permissions.contains(permission) || inheritPermissionsFrom.any { it.hasPermission(permission) }

    fun toDTO(): RoseRoleDTO {
        return RoseRoleDTO(
            id,
            weight,
            safeName,
            displayName,
            inheritPermissionsFrom.map { it.id },
            permissions.map { it.value })
    }
}

@Serializable
data class RoseRoleDTO(
    val id: Int,
    val weight: Int,
    val safeName: String,
    val displayName: String,
    val inheritPermissionsFrom: Collection<Int>,
    val permissions: Collection<Int>
)
