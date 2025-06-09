plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":data"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
