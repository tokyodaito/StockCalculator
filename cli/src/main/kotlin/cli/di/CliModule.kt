package cli.di

import cli.CliRunner
import org.koin.core.qualifier.named
import org.koin.dsl.module
import service.di.Runner

val cliModule =
    module {
        single<Runner>(qualifier = named("cli")) { CliRunner(get()) }
    }
