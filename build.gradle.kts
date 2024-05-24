val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
}

group = "cc.sapphiretech"
version = "0.0.1"

application {
    mainClass.set("cc.sapphiretech.rose.EntryKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")

    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.michael-bull.kotlin-result:kotlin-result:2.0.0")

    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.insert-koin:koin-ktor:3.5.6")
    testImplementation("io.insert-koin:koin-test-junit4:3.5.6")

    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.flywaydb:flyway-core:10.13.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.13.0")

    implementation("com.github.lilyrrose:argon-dsl:82014fcb69")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.11")
}

kotlin.sourceSets.all {
    languageSettings.optIn("io.ktor.server.locations.KtorExperimentalLocationsAPI")
}

