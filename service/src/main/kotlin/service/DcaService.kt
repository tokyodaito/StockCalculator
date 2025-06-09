package service

import data.market.MarketData
import service.dto.Action
import service.dto.Portfolio
import service.dto.StrategyConfig
import java.time.LocalDate

class DcaService(
    private val fetchMarketData: suspend () -> MarketData,
) {
    suspend fun generateReport(
        date: LocalDate,
        portfolio: Portfolio,
        config: StrategyConfig,
    ): String {
        val market = fetchMarketData()
        val filters = Strategy.getFilterStatuses(market, portfolio, config)
        val actions = Strategy.evaluate(date, market, portfolio, config)

        val lines =
            buildList {
                add("price=${market.price}, Δ=${"%.1f".format(Strategy.delta(market))}%")
                addAll(filters.map { "${it.name}: ${if (it.passed) "✔" else "✘"}" })
                if (actions.isEmpty()) add("Покупки не требуются")
                actions.forEach { action ->
                    when (action) {
                        is Action.Dca -> add("DCA: ${config.baseDcaAmount}")
                        is Action.Enhanced -> add("Усиленный транш: ${action.amount}")
                    }
                }
            }
        return lines.joinToString(separator = "\n")
    }
}
