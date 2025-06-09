plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":cli"))
    implementation(project(":telegram"))
    implementation(project(":service"))
    implementation(project(":data"))
}

application {
    mainClass.set("app.MainKt")
}
