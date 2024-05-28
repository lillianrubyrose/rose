package cc.sapphiretech.rose.routes

import cc.sapphiretech.rose.models.RosePermission
import cc.sapphiretech.rose.randomUsername
import cc.sapphiretech.rose.roseTest
import io.ktor.client.call.*
import io.ktor.http.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoleTest {
    @Test
    fun validCreate() = roseTest {
        val admin = registerSuperuser()
        val res = authPost(
            admin.id,
            "/role",
            requiredPermissions = arrayOf(RosePermission.MANAGE_ROLES),
            body = RoleCreate(randomUsername())
        )
        assertEquals(HttpStatusCode.Created, res.status, res.body())
    }

    @Test
    fun invalidCreate_BadName() = roseTest {
        val admin = registerSuperuser()
        suspend fun ensureDoesntWork(name: String) {
            val res = authPost(admin.id, "/role", skipAuthCheck = true, body = RoleCreate(name))
            assertEquals(HttpStatusCode.UnprocessableEntity, res.status, res.body())
        }

        ensureDoesntWork("a")
        ensureDoesntWork("user name")
        ensureDoesntWork("username!")
        ensureDoesntWork("really_long_username_123456789")
        ensureDoesntWork("🏳️‍⚧️🏳️‍⚧️🏳️‍⚧️🏳️‍⚧️")
        ensureDoesntWork("hello-world")
    }

    @Test
    fun validGetAll() = roseTest {
        createRandomRole()

        val admin = registerSuperuser()
        val res = authGet(admin.id, "/role")
        assertEquals(HttpStatusCode.OK, res.status)
        assertTrue(res.body<RoleGetAllResponse>().roles.isNotEmpty())
    }
}