package cc.sapphiretech.rose.db

import org.jetbrains.exposed.sql.Table

object RolesTable : Table("roles") {
    val id = integer("id")
    val weight = integer("weight")
    val safeName = text("safe_name")
    val displayName = text("display_name")
    val inheritPermissionsFrom = array<Int>("inherit_permissions_from")
    val permissions = array<Int>("permissions")
}