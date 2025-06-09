package bot

sealed interface BotResponse

data class TextResponse(val text: String) : BotResponse

data class WebAppResponse(val url: String) : BotResponse
