package webapp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import service.DcaService
import service.dto.Portfolio
import service.dto.StrategyConfig
import java.time.LocalDate

@RestController
class ReportController(
    private val service: DcaService,
) {
    @GetMapping("/api/report")
    suspend fun report(): String {
        val portfolio = Portfolio(700_000.0, 300_000.0, 300_000.0)
        val config = StrategyConfig()
        return service.generateReport(LocalDate.now(), portfolio, config)
    }
}
