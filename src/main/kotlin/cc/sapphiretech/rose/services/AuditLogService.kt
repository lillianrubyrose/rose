package cc.sapphiretech.rose.services

import cc.sapphiretech.rose.ext.lazyInject
import cc.sapphiretech.rose.models.LoginAuditLogEntry
import cc.sapphiretech.rose.models.RoleCreateAuditLogEntry
import cc.sapphiretech.rose.models.RoseRole
import cc.sapphiretech.rose.models.RoseUser
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.Document

class AuditLogService {
    private val mongoDb by lazyInject<MongoDatabase>()
    private val collection = mongoDb.getCollection<Document>("audit_logs")

    suspend fun onLogin(userId: Int) {
        collection.insertOne(LoginAuditLogEntry(userId).toDocument())
    }

    suspend fun onRoleCreate(creatorId: Int, role: RoseRole) {
        collection.insertOne(
            RoleCreateAuditLogEntry(
                System.currentTimeMillis(),
                creatorId,
                role.safeName,
                role.id
            ).toDocument()
        )
    }
}