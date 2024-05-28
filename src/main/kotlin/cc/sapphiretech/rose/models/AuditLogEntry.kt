package cc.sapphiretech.rose.models

import org.bson.Document
import org.bson.types.ObjectId

enum class AuditLogType {
    UserLogin,
    RoleCreate
}

open class AuditLogEntry(open val id: ObjectId = ObjectId(), private val type: AuditLogType) {
    companion object {
        const val MONGO_TYPE_FIELD: String = "\$__type__\$"
    }

    open fun toDocument(): Document = Document("_id", id).append(MONGO_TYPE_FIELD, type.name)
}

data class LoginAuditLogEntry(val userId: Int) : AuditLogEntry(type = AuditLogType.UserLogin) {
    override fun toDocument(): Document = super.toDocument().append("user", userId)
}

data class RoleCreateAuditLogEntry(
    val timestamp: Long,
    val creatorId: Int,
    val initialRoleName: String,
    val roleId: Int,
) : AuditLogEntry(type = AuditLogType.RoleCreate) {
    override fun toDocument(): Document = super.toDocument().append("timestamp", timestamp).append("creator", creatorId)
        .append("initialRoleName", initialRoleName).append("roleId", roleId)
}
