package cc.sapphiretech.rose.services

import cc.sapphiretech.rose.db.UsersTable
import cc.sapphiretech.rose.ext.transaction
import cc.sapphiretech.rose.models.BasicWebResponse
import cc.sapphiretech.rose.models.RoseUser
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import pm.lily.argon.argonHash

class UserService {
    suspend fun findById(id: Int): RoseUser? = transaction {
        UsersTable.selectAll().where(UsersTable.id.eq(id)).limit(1).map {
            RoseUser(
                it[UsersTable.id],
                it[UsersTable.safeUsername],
                it[UsersTable.displayUsername],
                it[UsersTable.passwordHash]
            )
        }.singleOrNull()
    }

    suspend fun findByUsername(username: String): Result<RoseUser?, CreateError> = transaction {
        if (!validUsername(username)) {
            return@transaction Err(CreateError.InvalidUsername)
        }

        Ok(UsersTable.selectAll().where(UsersTable.safeUsername.eq(username.lowercase())).limit(1).map {
            RoseUser(
                it[UsersTable.id],
                it[UsersTable.safeUsername],
                it[UsersTable.displayUsername],
                it[UsersTable.passwordHash]
            )
        }.singleOrNull())
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
            UsersTable.select(UsersTable.id).where(UsersTable.safeUsername.eq(safeUsername)).limit(1).singleOrNull() != null
        }
        if (exists) {
            return Err(CreateError.UsernameTaken)
        }

        val passwordHash = password.argonHash()
        val userId = transaction {
            UsersTable.insertReturning(listOf(UsersTable.id)) {
                it[UsersTable.safeUsername] = safeUsername
                it[displayUsername] = username
                it[UsersTable.passwordHash] = passwordHash
            }.single()
        }[UsersTable.id]

        return Ok(RoseUser(userId, safeUsername, username, passwordHash))
    }

    private fun validUsername(username: String): Boolean {
        return !(username.length > 16 || username.length < 2 || username.any { !it.isDigit() && !it.isLetter() && it != '_' })
    }

    enum class CreateError {
        InvalidUsername,
        InvalidPassword,

        UsernameTaken;

        fun toWebError(): BasicWebResponse {
            return when (this) {
                UsernameTaken -> BasicWebResponse("USERNAME_TAKEN")
                InvalidUsername -> BasicWebResponse("INVALID_USERNAME")
                InvalidPassword -> BasicWebResponse("INVALID_PASSWORD")
            }
        }
    }
}