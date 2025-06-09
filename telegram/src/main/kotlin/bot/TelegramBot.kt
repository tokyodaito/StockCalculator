package bot

import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo
import org.telegram.telegrambots.meta.api.objects.Update
import service.dto.Portfolio

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
            val response =
                runBlocking {
                    handler.handle(chatId, text, portfolioProvider())
                }
            val message = when (response) {
                is TextResponse -> SendMessage(chatId.toString(), response.text)
                is WebAppResponse -> {
                    val button =
                        InlineKeyboardButton("Open WebApp").apply {
                            webApp = WebAppInfo(response.url)
                        }
                    SendMessage(chatId.toString(), "Запустить WebApp").apply {
                        replyMarkup = InlineKeyboardMarkup(listOf(listOf(button)))
                    }
                }
            }
            execute(message)
        }
    }
}
