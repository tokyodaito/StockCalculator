package cli

import kotlinx.coroutines.runBlocking
import service.DcaService
import service.di.Runner
import service.dto.Portfolio
import service.dto.StrategyConfig

class CliRunner(
    private val service: DcaService,
) : Runner {
    override fun run(chatId: Long?) =
        runBlocking {
            val portfolio = Portfolio(equity = 700_000.0, others = 300_000.0, cushionAmount = 300_000.0)
            val config = StrategyConfig()
            val report =
                service.generateReport(
                    date = java.time.LocalDate.now(),
                    portfolio = portfolio,
                    config = config,
                )
            println(report)
        }
}
