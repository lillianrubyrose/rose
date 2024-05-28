package cc.sapphiretech.rose.services

import cc.sapphiretech.rose.db.UsersTable
import cc.sapphiretech.rose.ext.lazyInject
import cc.sapphiretech.rose.ext.transaction
import cc.sapphiretech.rose.ksp.GenericEnumError
import cc.sapphiretech.rose.models.RoseRole
import cc.sapphiretech.rose.models.RoseUser
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import pm.lily.argon.argonHash

private suspend fun ResultRow.toRoseUser(roleService: RoleService): RoseUser {
    val roles = this[UsersTable.roles].mapNotNullTo(mutableSetOf()) { id ->
        roleService.findById(id)
    }

    return RoseUser(
        this[UsersTable.id],
        this[UsersTable.superuser],
        this[UsersTable.safeUsername],
        this[UsersTable.displayUsername],
        this[UsersTable.passwordHash],
        roles
    )
}

private suspend fun Query.mapToRoseUser(roleService: RoleService): Iterable<RoseUser> {
    return map { it.toRoseUser(roleService) }
}

class UserService {
    private val roleService by lazyInject<RoleService>()

    companion object {
        @JvmStatic
        fun validUsername(username: String): Boolean {
            return !(username.length > 16 || username.length < 2 || username.any { !it.isDigit() && !it.isLetter() && it != '_' })
        }
    }

    suspend fun findById(id: Int): RoseUser? = transaction {
        UsersTable.selectAll().where(UsersTable.id.eq(id)).limit(1).mapToRoseUser(roleService).singleOrNull()
    }

    suspend fun findByUsername(username: String): Result<RoseUser?, CreateError> = transaction {
        if (!validUsername(username)) {
            return@transaction Err(CreateError.InvalidUsername)
        }

        Ok(
            UsersTable.selectAll().where(UsersTable.safeUsername.eq(username.lowercase())).limit(1)
                .mapToRoseUser(roleService)
                .singleOrNull()
        )
    }

    suspend fun create(username: String, password: String): Result<RoseUser, CreateError> {
        if (!validUsername(username)) {
            return Err(CreateError.InvalidUsername)
        }

        if (password.length < 8) {
            return Err(CreateError.InvalidPassword)
        }

        val safeUsername = username.lowercase()
        val exists = transaction {
            UsersTable.select(UsersTable.id).where(UsersTable.safeUsername.eq(safeUsername)).limit(1)
                .singleOrNull() != null
        }
        if (exists) {
            return Err(CreateError.UsernameTaken)
        }

        val passwordHash = password.argonHash()
        val user = transaction {
            UsersTable.insertReturning(UsersTable.columns) {
                it[UsersTable.safeUsername] = safeUsername
                it[displayUsername] = username
                it[UsersTable.passwordHash] = passwordHash
            }.single()
        }.toRoseUser(roleService)

        return Ok(user)
    }

    suspend fun addRole(user: RoseUser, role: RoseRole): RoseUser {
        user.roles.add(role)
        transaction {
            UsersTable.update({ UsersTable.id.eq(user.id) }) {
                it[roles] = user.roles.map { r -> r.id }
            }
        }
        return user
    }

    suspend fun removeRole(user: RoseUser, role: RoseRole): RoseUser {
        user.roles.remove(role)
        transaction {
            UsersTable.update({ UsersTable.id.eq(user.id) }) {
                it[roles] = user.roles.map { r -> r.id }
            }
        }
        return user
    }

    @GenericEnumError
    enum class CreateError {
        InvalidUsername,
        InvalidPassword,

        UsernameTaken;
    }
}