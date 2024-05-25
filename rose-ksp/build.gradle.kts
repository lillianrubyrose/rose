plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

group = "cc.sapphiretech"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.turingcomplete:text-case-converter:2.0.0")

    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")
    implementation("com.google.auto.service:auto-service-annotations:1.0")
    ksp("dev.zacsweers.autoservice:auto-service-ksp:0.5.2")
}
