package bot

import org.koin.dsl.module
import service.di.Runner
import service.dto.Portfolio

val telegramModule =
    module {
        single<Runner>(
            qualifier =
                org.koin.core.qualifier
                    .named("bot"),
        ) {
            val token = System.getenv("TELEGRAM_BOT_TOKEN") ?: error("TELEGRAM_BOT_TOKEN not set")
            val username = System.getenv("TELEGRAM_BOT_USERNAME") ?: "StockBot"
            val webAppUrl = System.getenv("WEBAPP_URL") ?: "http://localhost:8080"
            TelegramRunner(token, username, webAppUrl) { Portfolio(700_000.0, 300_000.0, 300_000.0) }
        }
    }
