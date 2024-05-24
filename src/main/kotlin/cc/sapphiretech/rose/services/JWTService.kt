package cc.sapphiretech.rose.services

import cc.sapphiretech.rose.Config
import cc.sapphiretech.rose.ext.lazyInject
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Duration
import java.time.Instant
import java.util.*

class JWTService {
    private val config by lazyInject<Config>()

    private val secret = config.jwt.secret.encodeToByteArray()
    private val domain = config.jwt.domain
    private val audience = config.jwt.audience
    val realm = config.jwt.realm

    val verifier: JWTVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(domain)
        .build()

    fun createAccessToken(userId: Int): String {
        return JWT.create()
            .withIssuer(domain)
            .withAudience(audience)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(
                Date.from(
                    Instant.now().plus(Duration.ofDays(7))
                )
            )
            .withSubject(userId.toString())
            .withClaim("t", System.nanoTime())
            .sign(Algorithm.HMAC256(secret))
    }
}