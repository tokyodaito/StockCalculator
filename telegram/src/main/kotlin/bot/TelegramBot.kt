package bot

import kotlinx.coroutines.runBlocking
import org.example.Portfolio
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class TelegramBot(
    private val token: String,
    private val username: String,
    private val handler: BotCommandHandler,
    private val portfolioProvider: () -> Portfolio,
) : TelegramLongPollingBot() {

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = username

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId
            val text = update.message.text
            val response = runBlocking {
                handler.handle(chatId, text, portfolioProvider())
            }
            val message = SendMessage(chatId.toString(), response)
            execute(message)
        }
    }
}
