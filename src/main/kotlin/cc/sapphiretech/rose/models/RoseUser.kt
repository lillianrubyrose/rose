package cc.sapphiretech.rose.models

import io.ktor.server.auth.*

data class RoseUser(val id: Int, val safeUsername: String, val displayName: String, val passwordHash: String) :
    Principal
