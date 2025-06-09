plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":service"))
    implementation(project(":data"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}

application {
    mainClass.set("cli.CliRunnerKt")
}
