package cc.sapphiretech.rose.plugins

import cc.sapphiretech.rose.Config
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import org.postgresql.ds.PGSimpleDataSource

fun Application.configureDatabase() {
    val config by inject<Config>()

    val pg = PGSimpleDataSource()
    pg.setUrl(config.postgres.uri)
    val flyway = Flyway.configure().dataSource(pg).load()
    flyway.migrate()

    Database.connect(config.postgres.uri)
}