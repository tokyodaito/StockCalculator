package app

import bot.telegramModule
import cli.di.cliModule
import data.dataModule
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import service.di.Runner
import service.di.serviceModule

fun main(args: Array<String>) {
    val mode = args.getOrNull(0) ?: "cli"
    val koin =
        startKoin {
            modules(dataModule, serviceModule, cliModule, telegramModule)
        }.koin
    val runner: Runner =
        when (mode) {
            "bot" -> koin.get(named("bot"))
            else -> koin.get(named("cli"))
        }
    runner.run(null)
}
