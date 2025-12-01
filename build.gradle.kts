plugins {
    kotlin("jvm") version "2.2.21"
    id("io.kotest") version "6.0.5"
}

group = "com.mintleaf"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    val kotestVersion = "6.0.5"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("ch.qos.logback:logback-classic:1.5.21")
}

kotlin { jvmToolchain(23) }

tasks.withType<Test> { useJUnitPlatform() }
