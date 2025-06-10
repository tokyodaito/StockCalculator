package service

import data.macro.MacroData
import data.market.MarketData
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

        val corridor: Corridor =
            when {
                // 1. Extreme Risk-Off
                macro.keyRate >= 22.0 || macro.brent <= 50.0 ->
                    Corridor(
                        riskLevel = "СИЛЬНЫЙ RISK-OFF",
                        percentageStocks = "45–60 %",
                    )

                // 2. Risk-Off
                macro.keyRate >= 18.0 ||
                    macro.keyRate - macro.keyRate6mAgo >= 1.0 ||
                    macro.brent <= 60.0 ->
                    Corridor(
                        riskLevel = "УМЕРЕННЫЙ RISK-OFF",
                        percentageStocks = "55–65 %",
                    )

                // 3. Neutral
                macro.keyRate in 15.0..17.99 &&
                    macro.brent in 60.0..75.0 ->
                    Corridor(
                        riskLevel = "НЕЙТРАЛЬНЫЙ РЕЖИМ",
                        percentageStocks = "60–70 %",
                    )

                // 4. Risk-On
                macro.keyRate <= 15.0 &&
                    macro.brent >= 75.0 &&
                    macro.keyRate6mAgo - macro.keyRate >= 1.5 ->
                    Corridor(
                        riskLevel = "RISK-ON",
                        percentageStocks = "70–80 %",
                    )

                // 5. По-умолчанию (неопределёнка)
                else ->
                    Corridor(
                        riskLevel = "НЕЙТРАЛЬНЫЙ РЕЖИМ",
                        percentageStocks = "60–70 %",
                    )
            }

        val lines =
            buildList {
                add("price=${market.price}, Δ=${"%.1f".format(Strategy.delta(market))}%")
                add("Уровень риска: ${corridor.riskLevel}")
                add("Коридор акций: ${corridor.percentageStocks}")
                addAll(filters.filterStatuses.map { "${it.name}: ${if (it.passed) "✔" else "✘"}" })
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

data class Corridor(
    val riskLevel: String,
    val percentageStocks: String,
)
