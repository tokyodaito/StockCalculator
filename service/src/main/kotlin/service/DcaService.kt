package service

import data.market.MarketData
import data.macro.MacroData
import service.dto.Action
import service.dto.Portfolio
import service.dto.StrategyConfig
import java.time.LocalDate

class DcaService(
    private val fetchMarketData: suspend () -> MarketData,
    private val fetchMacroData: suspend (LocalDate) -> MacroData,
) {
    suspend fun generateReport(
        date: LocalDate,
        portfolio: Portfolio,
        config: StrategyConfig,
    ): String {
        val market = fetchMarketData()
        val macro = fetchMacroData(date)
        val filters = Strategy.getFilterStatuses(market, portfolio, config)
        val actions = Strategy.evaluate(date, market, portfolio, config)

        val corridor =
            when {
                macro.brent >= 70 && macro.keyRate6mAgo - macro.keyRate >= 0.5 -> "70–80 %"
                macro.brent <= 55 || macro.keyRate - macro.keyRate6mAgo >= 0.75 -> "55–70 %"
                else -> "n/a"
            }

        val lines =
            buildList {
                add("price=${market.price}, Δ=${"%.1f".format(Strategy.delta(market))}%")
                add("Коридор акций: $corridor")
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
