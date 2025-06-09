package bot

import data.ChatConfigRepository
import service.DcaService
import service.dto.Portfolio
import service.utils.toStrategyConfig
import java.time.LocalDate
import java.time.LocalTime

class BotCommandHandler(
    private val repository: ChatConfigRepository,
    private val dcaService: DcaService,
    private val webAppUrl: String,
) {
    suspend fun handle(
        chatId: Long,
        text: String,
        portfolio: Portfolio,
        date: LocalDate = LocalDate.now(),
    ): BotResponse {
        val parts = text.trim().split(Regex("\\s+"))
        val command = parts.firstOrNull() ?: return TextResponse("")
        val arg = parts.getOrNull(1)
        val config = repository.getConfig(chatId)
        return when (command) {
            "/set_monthly_flow" -> {
                val value = arg?.toDoubleOrNull() ?: return TextResponse("Неверный формат")
                if (value <= 0) return TextResponse("Значение должно быть положительным")
                config.monthlyFlow = value
                repository.update(chatId, config)
                TextResponse("Monthly flow установлен: ${value.toLong()}")
            }
            "/set_base_dca" -> {
                val value = arg?.toDoubleOrNull() ?: return TextResponse("Неверный формат")
                if (value <= 0 || value > config.monthlyFlow) return TextResponse("Значение должно быть положительным и ≤ monthly flow")
                config.baseDcaAmount = value
                repository.update(chatId, config)
                TextResponse("Base DCA установлен: ${value.toLong()}")
            }
            "/set_min_cushion_ratio" -> {
                val value = arg?.toDoubleOrNull() ?: return TextResponse("Неверный формат")
                if (value < 1.0) return TextResponse("Значение должно быть ≥ 1")
                config.minCushionRatio = value
                repository.update(chatId, config)
                TextResponse("Min cushion ratio = $value")
            }
            "/set_report_time" -> {
                val time =
                    try {
                        LocalTime.parse(arg)
                    } catch (_: Exception) {
                        return TextResponse("Неверный формат времени")
                    }
                config.reportTime = time
                repository.update(chatId, config)
                TextResponse("Report time установлен: $time")
            }
            "/show_config" -> {
                TextResponse(
                    """
                MONTHLY_FLOW=${config.monthlyFlow.toLong()}
BASE_DCA=${config.baseDcaAmount.toLong()}
MIN_CUSHION_RATIO=${config.minCushionRatio}
REPORT_TIME=${config.reportTime}
                """.trimIndent(),
                )
            }
            "/report_now" -> {
                TextResponse(
                    dcaService.generateReport(date, portfolio, config.toStrategyConfig()),
                )
            }
            "/open_webapp" -> {
                WebAppResponse(webAppUrl)
            }
            else -> TextResponse("Неизвестная команда")
        }
    }
}
