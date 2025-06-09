plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "org.example"
version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

application {
    mainClass.set("app.MainKt")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>().configureEach {
    manifest.attributes(mapOf("Main-Class" to "app.MainKt"))
}


tasks.named("build") {
    dependsOn("shadowJar")
}

subprojects {
    repositories { mavenCentral() }
}

application {
    mainClass.set("app.MainKt")
}
