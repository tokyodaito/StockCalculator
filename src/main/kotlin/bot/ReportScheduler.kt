package bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.Portfolio
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class ReportScheduler(
    private val repository: ChatConfigRepository,
    private val service: DcaService,
    private val sendMessage: suspend (Long, String) -> Unit,
    private val portfolioProvider: () -> Portfolio,
) {
    fun start(scope: CoroutineScope) = scope.launch {
        val zone = ZoneId.of("Europe/Moscow")
        while (isActive) {
            val now = LocalTime.now(zone)
            repository.allChatIds().forEach { id ->
                val config = repository.getConfig(id)
                if (now.hour == config.reportTime.hour && now.minute == config.reportTime.minute) {
                    val text = service.generateReport(LocalDate.now(zone), portfolioProvider(), config.toStrategyConfig())
                    sendMessage(id, text)
                }
            }
            delay(Duration.ofMinutes(1).toMillis())
        }
    }
}
