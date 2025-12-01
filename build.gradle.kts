plugins {
    kotlin("jvm") version "2.2.21"
    id("io.kotest") version "6.0.5"
    `maven-publish`
}

group = "com.biewers2"

version = "1.1-SNAPSHOT"

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name = "Coprocess"
                description =
                    "Coprocess is a Kotlin library for running programs asynchronously through coroutine suspension."
                url = "https://github.com/biewers2/coproc"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "biewers2"
                        name = "Jake Biewer"
                        email = "biewers2@gmail.com"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/biewers2/coproc.git"
                    developerConnection = "scm:git:ssh://github.com/biewers2/coproc.git"
                    url = "http://github.com/biewers2/coproc/"
                }
            }
        }
    }
}

tasks.withType<Test> { useJUnitPlatform() }
