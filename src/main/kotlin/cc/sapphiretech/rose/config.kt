package cc.sapphiretech.rose

import org.jetbrains.annotations.TestOnly

data class PostgresConfig(val uri: String)
data class MongoConfig(val uri: String)

data class JWTConfig(
    val secret: String,
    val domain: String,
    val audience: String,
    val realm: String
)

enum class Environment {
    Dev,
    Test,
    Prod;

    companion object {
        fun fromString(str: String): Environment {
            return when (str) {
                "dev" -> Dev
                "test" -> Test
                "prod" -> Prod
                else -> throw IllegalArgumentException("Unknown environment: $str")
            }
        }
    }
}

data class Config(
    val environment: Environment,
    val bindIp: String,
    val port: Int,
    val postgres: PostgresConfig,
    val mongo: MongoConfig,
    val jwt: JWTConfig
) {
    companion object {
        fun fromEnv() = Config(
            Environment.fromString(System.getenv("ROSE_ENVIRONMENT")),
            System.getenv("ROSE_BIND_IP"),
            System.getenv("ROSE_PORT").toInt(),
            PostgresConfig(System.getenv("ROSE_POSTGRES_URI")),
            MongoConfig(System.getenv("ROSE_MONGO_URI")),
            JWTConfig(
                System.getenv("ROSE_JWT_SECRET"),
                System.getenv("ROSE_JWT_DOMAIN"),
                System.getenv("ROSE_JWT_AUDIENCE"),
                System.getenv("ROSE_JWT_REALM")
            )
        )

        @TestOnly
        fun forTest() = Config(
            Environment.Test,
            "0.0.0.0",
            0,
            PostgresConfig("jdbc:postgresql://localhost:5433/rose_test?user=rose&password=password"),
            MongoConfig("mongodb://root:example@localhost:27018"),
            JWTConfig(
                "wawawawawawawawawa",
                "localhost",
                "localhost",
                "rose"
            )
        )
    }
}
