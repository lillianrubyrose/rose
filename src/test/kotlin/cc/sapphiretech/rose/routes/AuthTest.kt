package cc.sapphiretech.rose.routes

import cc.sapphiretech.rose.randomUsername
import cc.sapphiretech.rose.roseTest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthTest {
    companion object {
        const val VALID_PASSWORD = "password"
    }

    @Test
    fun validLogin() = roseTest {
        val user = registerRandomUser()
        val res = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthLogin(user.safeUsername, VALID_PASSWORD))
        }
        assertEquals(HttpStatusCode.OK, res.status, res.body())
    }

    @Test
    fun invalidLogin_BadUsername() = roseTest {
        val res = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthLogin(randomUsername(), VALID_PASSWORD))
        }
        assertEquals(HttpStatusCode.Unauthorized, res.status, res.body())
    }

    @Test
    fun invalidLogin_BadPassword() = roseTest {
        val user = registerRandomUser()
        val res = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthLogin(user.safeUsername, "meowmewowmewo"))
        }
        assertEquals(HttpStatusCode.Unauthorized, res.status, res.body())
    }

    @Test
    fun validRegistration() = roseTest {
        val res = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(AuthRegister(randomUsername(), VALID_PASSWORD))
        }
        assertEquals(HttpStatusCode.OK, res.status, res.body())
    }

    @Test
    fun invalidRegistration_BadUsername() = roseTest {
        suspend fun ensureDoesntWork(name: String) {
            val res = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(AuthRegister(name, VALID_PASSWORD))
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

    @Test
    fun invalidRegistration_BadPassword() = roseTest {
        val res = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(AuthRegister(randomUsername(), "abc123"))
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, res.status, res.body())
    }
}