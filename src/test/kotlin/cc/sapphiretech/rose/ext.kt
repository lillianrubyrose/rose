package cc.sapphiretech.rose

import cc.sapphiretech.rose.db.UsersTable
import cc.sapphiretech.rose.ext.lazyInject
import cc.sapphiretech.rose.ext.transaction
import cc.sapphiretech.rose.models.RoseUser
import cc.sapphiretech.rose.routes.AuthTest.Companion.VALID_PASSWORD
import cc.sapphiretech.rose.routes.AuthRegister
import cc.sapphiretech.rose.services.JWTService
import cc.sapphiretech.rose.services.UserService
import com.github.michaelbull.result.getOrThrow
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.update
import kotlin.random.Random
import kotlin.test.assertEquals

class RoseTestBuilder(clientProvider: ClientProvider) {
    val client = clientProvider.createClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private val userService by lazyInject<UserService>()
    private val jwtService by lazyInject<JWTService>()

    suspend fun registerRandomUser(): RoseUser {
        val username = randomUsername()
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(AuthRegister(username, VALID_PASSWORD))
        }
        return userService.findByUsername(username)
            .getOrThrow { RuntimeException("randomUsername() returned an invalid username") } ?: throw RuntimeException(
            "Couldn't find newly registered user '${username}. Something bad is going on!!"
        )
    }

    suspend fun registerAdministrator(): RoseUser {
        val user = registerRandomUser()
        user.superuser = true

        transaction {
            UsersTable.update({ UsersTable.id.eq(user.id) }) {
                it[superuser] = true
            }
        }

        return user
    }

    private suspend fun ensureRequiresAuthorization(path: String, method: HttpMethod, body: Any? = null) {
        val res = client.request(path) {
            this.method = method
            if (method != HttpMethod.Get && body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        assertEquals(HttpStatusCode.Unauthorized, res.status)
    }

    suspend fun authGet(asUser: Int, path: String, skipAuthCheck: Boolean = false, block: HttpRequestBuilder.() -> Unit): HttpResponse {
        if(!skipAuthCheck) {
            ensureRequiresAuthorization(path, HttpMethod.Get)
        }

        val token = jwtService.createAccessToken(asUser)
        return client.post(path) {
            header(HttpHeaders.Authorization, "Bearer $token")
            block()
        }
    }

    suspend fun authPost(asUser: Int, path: String, skipAuthCheck: Boolean = false, block: HttpRequestBuilder.() -> Unit): HttpResponse {
        if(!skipAuthCheck) {
            ensureRequiresAuthorization(path, HttpMethod.Post)
        }

        val token = jwtService.createAccessToken(asUser)
        return client.post(path) {
            header(HttpHeaders.Authorization, "Bearer $token")
            block()
        }
    }
}

fun roseTest(block: suspend RoseTestBuilder.() -> Unit) = testApplication {
    application {
        configureApp(Config.forTest())
    }

    block(RoseTestBuilder(this))
}

fun randomUsername(length: Int = 12): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z')
    return (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

