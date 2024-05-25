package cc.sapphiretech.rose.routes

import cc.sapphiretech.rose.randomUsername
import cc.sapphiretech.rose.roseTest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.junit.Test
import kotlin.test.assertEquals

class RoleTest {
    @Test
    fun validCreate() = roseTest {
        val admin = registerAdministrator()
        val res = authPost(admin.id, "/role") {
            contentType(ContentType.Application.Json)
            setBody(RoleCreate(randomUsername()))
        }
        assertEquals(HttpStatusCode.Created, res.status)
    }

    @Test
    fun invalidCreate_BadName() = roseTest {
        val admin = registerAdministrator()
        suspend fun ensureDoesntWork(name: String) {
            val res = authPost(admin.id, "/role", skipAuthCheck = true) {
                contentType(ContentType.Application.Json)
                setBody(RoleCreate(name))
            }

            assertEquals(HttpStatusCode.UnprocessableEntity, res.status, res.body())
        }

        ensureDoesntWork("a")
        ensureDoesntWork("user name")
        ensureDoesntWork("username!")
        ensureDoesntWork("really_long_username_123456789")
        ensureDoesntWork("🏳️‍⚧️🏳️‍⚧️🏳️‍⚧️🏳️‍⚧️")
        ensureDoesntWork("hello-world")
    }
}