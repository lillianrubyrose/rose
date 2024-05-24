package cc.sapphiretech.rose.db

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = integer("id")
    val safeUsername = text("safe_username")
    val displayUsername = text("display_username")
    val passwordHash = text("password_hash")
}