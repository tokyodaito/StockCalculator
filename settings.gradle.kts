plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "StockCalculator"

include("data", "service", "cli", "telegram", "webapp", "app")
