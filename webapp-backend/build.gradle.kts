plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    implementation(project(":service"))
    implementation(project(":data"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
