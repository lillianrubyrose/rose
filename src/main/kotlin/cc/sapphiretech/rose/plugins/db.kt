package cc.sapphiretech.rose.plugins

import cc.sapphiretech.rose.Config
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.Module
import org.koin.dsl.module
import org.postgresql.ds.PGSimpleDataSource

fun configureDatabase(config: Config): Module {
    val pg = PGSimpleDataSource()
    pg.setUrl(config.postgres.uri)
    val flyway = Flyway.configure().dataSource(pg).load()
    flyway.migrate()

    val exposedDb = Database.connect(config.postgres.uri)
    val mongoClient = MongoClient.create(config.mongo.uri)
    val mongoDb = mongoClient.getDatabase("rose")
    return module {
        single { exposedDb }
        single { mongoClient }
        single { mongoDb }
    }
}