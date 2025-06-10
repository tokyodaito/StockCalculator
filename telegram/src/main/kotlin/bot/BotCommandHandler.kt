package bot

import data.ChatConfigRepository
import data.OnboardingStage
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
        val trimmed = text.trim()
        val config = repository.getConfig(chatId)

        if (trimmed.equals("/start", ignoreCase = true)) {
            config.onboardingStage = OnboardingStage.WAIT_START
            repository.update(chatId, config)
            return TextResponse("Привет! Это бот для DCA стратегии. Напишите \"Начать\" для настройки")
        }

        when (config.onboardingStage) {
            OnboardingStage.WAIT_START -> {
                if (trimmed.equals("начать", ignoreCase = true)) {
                    config.onboardingStage = OnboardingStage.WAIT_EXPENSES
                    repository.update(chatId, config)
                    return TextResponse("Введите ваши ежемесячные расходы")
                }
                return TextResponse("Напишите \"Начать\" для продолжения")
            }
            OnboardingStage.WAIT_EXPENSES -> {
                val value = trimmed.toDoubleOrNull() ?: return TextResponse("Неверный формат")
                config.expenses = value
                config.onboardingStage = OnboardingStage.WAIT_CUSHION_CONFIRM
                repository.update(chatId, config)
                val cushion = (value * 3).toLong()
                return TextResponse("Ваша подушка = ${cushion} ₽\nВведите 'есть' если подушка готова или 'нет'")
            }
            OnboardingStage.WAIT_CUSHION_CONFIRM -> {
                if (trimmed.equals("есть", true) || trimmed.equals("нет", true)) {
                    config.onboardingStage = OnboardingStage.WAIT_MONTHLY_FLOW
                    repository.update(chatId, config)
                    return TextResponse("Сколько откладываете в месяц?")
                }
                return TextResponse("Введите 'есть' или 'нет'")
            }
            OnboardingStage.WAIT_MONTHLY_FLOW -> {
                val value = trimmed.toDoubleOrNull() ?: return TextResponse("Неверный формат")
                if (value <= 0) return TextResponse("Значение должно быть положительным")
                config.flow = value
                config.onboardingStage = OnboardingStage.WAIT_MODEL
                repository.update(chatId, config)
                return TextResponse("Выберите модель: Консервативная 40/60, Сбалансированная 50/50, Агрессивная 60/40")
            }
            OnboardingStage.WAIT_MODEL -> {
                val ratio = when (trimmed.lowercase()) {
                    "консервативная" -> 0.4
                    "сбалансированная" -> 0.5
                    "агрессивная" -> 0.6
                    else -> null
                } ?: return TextResponse("Выберите модель из списка")
                config.monthlyFlow = config.flow
                config.baseDcaAmount = config.monthlyFlow * ratio
                config.onboardingStage = OnboardingStage.WAIT_REPORT_TIME
                repository.update(chatId, config)
                val reserve = (config.monthlyFlow - config.baseDcaAmount).toLong()
                val dca = config.baseDcaAmount.toLong()
                return TextResponse("DCA = ${dca} ₽, Резерв = ${reserve} ₽\nВо сколько проверять рынок? (по умолчанию 10:00)")
            }
            OnboardingStage.WAIT_REPORT_TIME -> {
                val time = if (trimmed.isEmpty()) {
                    LocalTime.of(10, 0)
                } else {
                    try {
                        LocalTime.parse(trimmed)
                    } catch (_: Exception) {
                        return TextResponse("Неверный формат времени")
                    }
                }
                config.reportTime = time
                config.onboardingStage = OnboardingStage.WAIT_PUSH
                repository.update(chatId, config)
                return TextResponse("Включить push-уведомления? да/нет")
            }
            OnboardingStage.WAIT_PUSH -> {
                if (trimmed.equals("да", true) || trimmed.equals("нет", true)) {
                    config.onboardingStage = OnboardingStage.NONE
                    repository.update(chatId, config)
                    return TextResponse("Настройка завершена")
                }
                return TextResponse("Ответьте да или нет")
            }
            OnboardingStage.NONE -> {}
        }

        if (!trimmed.startsWith("/")) return TextResponse("Неизвестная команда")

        val parts = trimmed.split(Regex("\\s+"))
        val command = parts.first()
        val arg = parts.getOrNull(1)
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
