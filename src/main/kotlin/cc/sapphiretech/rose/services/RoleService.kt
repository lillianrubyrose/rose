package cc.sapphiretech.rose.services

import cc.sapphiretech.rose.db.RolesTable
import cc.sapphiretech.rose.ext.lazyInject
import cc.sapphiretech.rose.ext.transaction
import cc.sapphiretech.rose.ksp.GenericEnumError
import cc.sapphiretech.rose.models.RosePermission
import cc.sapphiretech.rose.models.RoseRole
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

private suspend fun ResultRow.mapToRoseRole(): RoseRole {
    val inheritance = this[RolesTable.inheritPermissionsFrom].mapNotNullTo(mutableSetOf()) { id ->
        transaction {
            RolesTable.selectAll().where(RolesTable.id.eq(id)).limit(1).mapToRoseRole().singleOrNull()
        }
    }

    val permissions = this[RolesTable.permissions].mapNotNullTo(mutableSetOf()) { p -> RosePermission.fromOrd(p) }

    return RoseRole(
        this[RolesTable.id],
        this[RolesTable.weight],
        this[RolesTable.safeName],
        this[RolesTable.displayName],
        inheritance,
        permissions
    )
}

private suspend fun Query.mapToRoseRole(): Iterable<RoseRole> {
    return map { it.mapToRoseRole() }
}

class RoleService {
    private val auditLogService by lazyInject<AuditLogService>()

    suspend fun findById(id: Int): RoseRole? = transaction {
        RolesTable.selectAll().where(RolesTable.id.eq(id)).limit(1).mapToRoseRole().singleOrNull()
    }

    suspend fun findByName(name: String): Result<RoseRole?, CreateError> = transaction {
        if (!validName(name)) {
            return@transaction Err(CreateError.InvalidName)
        }

        Ok(
            RolesTable.selectAll().where(RolesTable.safeName.eq(name.lowercase())).limit(1)
                .mapToRoseRole()
                .singleOrNull()
        )
    }

    suspend fun getAll(): List<RoseRole> {
        return transaction { RolesTable.selectAll().map { it.mapToRoseRole() } }
    }

    suspend fun create(creatorId: Int, name: String): Result<RoseRole, CreateError> {
        if (!validName(name)) {
            return Err(CreateError.InvalidName)
        }

        val safeName = name.lowercase()
        val exists = transaction {
            RolesTable.select(RolesTable.id).where(RolesTable.safeName.eq(safeName)).limit(1)
                .singleOrNull() != null
        }
        if (exists) {
            return Err(CreateError.NameTaken)
        }

        val role = transaction {
            RolesTable.insertReturning(RolesTable.columns) {
                it[weight] = 0
                it[RolesTable.safeName] = safeName
                it[displayName] = name
            }.single()
        }.mapToRoseRole()

        auditLogService.onRoleCreate(creatorId, role)
        return Ok(role)
    }

    suspend fun addPermissions(role: RoseRole, vararg permissions: RosePermission): RoseRole {
        role.permissions.addAll(permissions)
        transaction {
            RolesTable.update({ RolesTable.id.eq(role.id) }) {
                it[RolesTable.permissions] = role.permissions.map { p -> p.value }
            }
        }

        return role
    }

    private fun validName(name: String): Boolean = UserService.validUsername(name)

    @GenericEnumError
    enum class CreateError {
        InvalidName,
        NameTaken,
    }
}