package cc.sapphiretech.rose

import cc.sapphiretech.rose.db.UsersTable
import cc.sapphiretech.rose.ext.lazyInject
import cc.sapphiretech.rose.ext.transaction
import cc.sapphiretech.rose.models.RosePermission
import cc.sapphiretech.rose.models.RoseRole
import cc.sapphiretech.rose.models.RoseUser
import cc.sapphiretech.rose.routes.AuthTest.Companion.VALID_PASSWORD
import cc.sapphiretech.rose.services.JWTService
import cc.sapphiretech.rose.services.RoleService
import cc.sapphiretech.rose.services.UserService
import com.github.michaelbull.result.getOrThrow
import com.github.michaelbull.result.map
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.update
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class RoseTestBuilder(clientProvider: ClientProvider) {
    val client = clientProvider.createClient {
        install(ContentNegotiation) {
            json()
        }
    }

    val userService by lazyInject<UserService>()
    private val jwtService by lazyInject<JWTService>()
    private val roleService by lazyInject<RoleService>()

    suspend fun registerRandomUser(permissions: Array<RosePermission>? = null): RoseUser {
        var name = randomUsername()
        while (userService.findByUsername(name).map { it != null }.getOrThrow { RuntimeException("DB Error") }) {
            name = randomUsername()
        }

        val user = userService.create(name, VALID_PASSWORD)
            .getOrThrow { RuntimeException("randomUsername() returned an invalid username: $name") }

        if (permissions != null) {
            val role = createRandomRole(permissions)
            return userService.addRole(user, role)
        }

        return user
    }

    suspend fun registerSuperuser(): RoseUser {
        val user = registerRandomUser()
        user.superuser = true

        transaction {
            UsersTable.update({ UsersTable.id.eq(user.id) }) {
                it[superuser] = true
            }
        }

        return user
    }

    suspend fun createRandomRole(permissions: Array<RosePermission>? = null): RoseRole {
        var name = randomUsername()
        while (roleService.findByName(name).map { it != null }.getOrThrow { RuntimeException("DB Error") }) {
            name = randomUsername()
        }

        val role = roleService.create(0, name)
            .getOrThrow { RuntimeException("randomUsername() returned an invalid name: $name") }

        if (permissions != null) {
            return roleService.addPermissions(role, *permissions)
        }

        return role
    }

    private suspend fun ensureRequiresAuthorization(
        path: String,
        method: HttpMethod,
        requiredPermissions: Array<RosePermission>? = null,
        body: Any? = null
    ) {
        val builder: HttpRequestBuilder.() -> Unit = {
            this.method = method
            if (method != HttpMethod.Get && body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

        val res = client.request(path, builder)
        assertEquals(HttpStatusCode.Unauthorized, res.status)

        if (requiredPermissions != null) {
            val unauthorizedUser = registerRandomUser()
            val unauthorizedRes = client.request(path) {
                builder()
                header(HttpHeaders.Authorization, "Bearer ${jwtService.createAccessToken(unauthorizedUser.id)}")
            }
            assertEquals(HttpStatusCode.Unauthorized, unauthorizedRes.status)

            val authorizedUser = registerRandomUser(requiredPermissions)
            val authorizedRes = client.request(path) {
                builder()
                header(HttpHeaders.Authorization, "Bearer ${jwtService.createAccessToken(authorizedUser.id)}")
            }
            assertNotEquals(HttpStatusCode.Unauthorized, authorizedRes.status)
        }
    }

    suspend fun authGet(
        asUser: Int,
        path: String,
        skipAuthCheck: Boolean = false,
        requiredPermissions: Array<RosePermission>? = null,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        val token = jwtService.createAccessToken(asUser)
        val res = client.get(path) {
            header(HttpHeaders.Authorization, "Bearer $token")
            block()
        }
        if (!skipAuthCheck) {
            ensureRequiresAuthorization(path, HttpMethod.Get, requiredPermissions)
        }
        return res
    }

    suspend fun authPost(
        asUser: Int,
        path: String,
        skipAuthCheck: Boolean = false,
        requiredPermissions: Array<RosePermission>? = null,
        body: Any?,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        val token = jwtService.createAccessToken(asUser)
        val res = client.post(path) {
            header(HttpHeaders.Authorization, "Bearer $token")
            block()
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (!skipAuthCheck) {
            ensureRequiresAuthorization(path, HttpMethod.Post, requiredPermissions, body)
        }
        return res
    }

    suspend fun authPut(
        asUser: Int,
        path: String,
        skipAuthCheck: Boolean = false,
        requiredPermissions: Array<RosePermission>? = null,
        body: Any?,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        val token = jwtService.createAccessToken(asUser)
        val res = client.put(path) {
            header(HttpHeaders.Authorization, "Bearer $token")
            block()
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (!skipAuthCheck) {
            ensureRequiresAuthorization(path, HttpMethod.Put, requiredPermissions, body)
        }
        return res
    }

    suspend fun authDelete(
        asUser: Int,
        path: String,
        skipAuthCheck: Boolean = false,
        requiredPermissions: Array<RosePermission>? = null,
        body: Any?,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        val token = jwtService.createAccessToken(asUser)
        val res = client.delete(path) {
            header(HttpHeaders.Authorization, "Bearer $token")
            block()
            if (body != null) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        if (!skipAuthCheck) {
            ensureRequiresAuthorization(path, HttpMethod.Delete, requiredPermissions, body)
        }
        return res
    }
}

fun roseTest(block: suspend RoseTestBuilder.() -> Unit) = testApplication {
    application {
        configureApp(Config.forTest())
    }

    // Have to do this so the application and by extension Koin gets initialized as RoseTestBuilder injects services.
    // Why the setup is done upon first request is beyond me.
    client.get("/")

    block(RoseTestBuilder(this))
}

fun randomUsername(length: Int = 12): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z')
    return (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

