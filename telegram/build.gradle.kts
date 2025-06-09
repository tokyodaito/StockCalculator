plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":service"))
    implementation(project(":data"))
    implementation(project(":webapp"))
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.insert-koin:koin-core:3.5.3")

    testImplementation(kotlin("test"))
}
