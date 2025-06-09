package bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import webapp.startWebApp
import service.DcaService
import service.di.Runner
import service.dto.Portfolio

class TelegramRunner(
    private val token: String,
    private val username: String,
    private val webAppUrl: String,
    private val portfolioProvider: () -> Portfolio,
) : Runner,
    KoinComponent {
    private val repository: data.ChatConfigRepository by inject()
    private val service: DcaService by inject()

    override fun run(chatId: Long?) {
        val handler = BotCommandHandler(repository, service, webAppUrl)
        startWebApp()
        val bot = TelegramBot(token, username, handler, portfolioProvider)
        val api = TelegramBotsApi(DefaultBotSession::class.java)
        api.registerBot(bot)
        val scheduler =
            ReportScheduler(repository, service, { id: Long, text: String ->
                bot.execute(
                    org.telegram.telegrambots.meta.api.methods.send
                        .SendMessage(id.toString(), text),
                )
            }, portfolioProvider)
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Default)
            scheduler.start(scope)
            kotlinx.coroutines.delay(Long.MAX_VALUE)
        }
    }
}
