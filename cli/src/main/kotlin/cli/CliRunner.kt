package cli

import kotlinx.coroutines.runBlocking
import org.example.Portfolio
import org.example.StrategyConfig
import service.DcaService
import org.example.di.AppModule

class CliRunner {
    fun run() = runBlocking {
        val portfolio = Portfolio(700_000.0, 300_000.0, 300_000.0)
        val config = StrategyConfig()
        val service = DcaService { AppModule.dataSource.fetchMarketData() }
        val report = service.generateReport(java.time.LocalDate.now(), portfolio, config)
        println(report)
    }
}

fun main() {
    CliRunner().run()
}
