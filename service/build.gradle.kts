plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":data"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.insert-koin:koin-core:3.5.3")

    testImplementation(kotlin("test"))
}
