plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":cli"))
    implementation(project(":telegram"))
    implementation(project(":service"))
    implementation(project(":data"))
    implementation("io.insert-koin:koin-core:3.5.3")
}

application {
    mainClass.set("app.MainKt")
}
