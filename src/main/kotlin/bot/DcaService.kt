package bot

import data.market.MarketData
import org.example.Portfolio
import org.example.Strategy
import org.example.StrategyConfig
import java.time.LocalDate

class DcaService(private val fetchMarketData: suspend () -> MarketData) {
    suspend fun generateReport(date: LocalDate, portfolio: Portfolio, config: StrategyConfig): String {
        val market = fetchMarketData()
        val filters = Strategy.getFilterStatuses(market, portfolio, config)
        val actions = Strategy.evaluate(date, market, portfolio, config)

        val lines = buildList {
            add("price=${market.price}, Δ=${"%.1f".format(Strategy.delta(market))}%")
            addAll(filters.map { "${it.name}: ${if (it.passed) "✔" else "✘"}" })
            if (actions.isEmpty()) add("Покупки не требуются")
            actions.forEach { action ->
                when (action) {
                    is org.example.Action.Dca -> add("DCA: ${config.baseDcaAmount}")
                    is org.example.Action.Enhanced -> add("Усиленный транш: ${action.amount}")
                }
            }
        }
        return lines.joinToString(separator = "\n")
    }
}
