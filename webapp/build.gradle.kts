plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":service"))
    implementation(project(":data"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.0")
    testImplementation(kotlin("test"))
}
