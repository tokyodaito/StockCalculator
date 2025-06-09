plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories { mavenCentral() }


tasks.named("build") {
    dependsOn("shadowJar")
}

application {
    mainClass.set("app.MainKt")
}

subprojects {
    repositories { mavenCentral() }
}
