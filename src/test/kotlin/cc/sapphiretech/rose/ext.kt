package cc.sapphiretech.rose

import cc.sapphiretech.rose.ext.lazyInject
import cc.sapphiretech.rose.models.RoseUser
import cc.sapphiretech.rose.routes.AuthKtTest.Companion.VALID_PASSWORD
import cc.sapphiretech.rose.routes.AuthRegister
import cc.sapphiretech.rose.services.UserService
import com.github.michaelbull.result.getOrThrow
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlin.random.Random

class RoseTestBuilder(clientProvider: ClientProvider) {
    val client = clientProvider.createClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private val userService by lazyInject<UserService>()

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

