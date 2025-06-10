package service

import data.market.MarketData
import service.dto.Action
import service.dto.FilterStatus
import service.dto.Filters
import service.dto.Portfolio
import service.dto.StrategyConfig
import java.time.LocalDate

internal object Strategy {
    fun delta(marketData: MarketData): Double = (marketData.price - marketData.max52) / marketData.max52 * 100.0

    fun enhancedTranche(
        delta: Double,
        sigma30: Double,
        config: StrategyConfig,
    ): Double {
        val k =
            when {
                delta <= -20.0 -> 2.0
                delta <= -10.0 -> 1.0
                else -> 0.0
            }
        if (k == 0.0) return 0.0
        val adjusted = (k * (0.10 / sigma30)).coerceIn(0.4, 2.5)
        return adjusted * config.baseDcaAmount
    }

    fun getFilterStatuses(
        marketData: MarketData,
        portfolio: Portfolio,
        config: StrategyConfig,
    ): Filters {
        val capeOk = marketData.cape <= 8.0
        val techOk = marketData.price >= marketData.sma200 || marketData.rsi14 < 30.0
        val crossOk = marketData.sma50 > marketData.sma200
        val cushionOk = portfolio.cushionAmount / config.monthlyFlow >= 3.0
        return Filters(
            marketData = marketData,
            filterStatuses =
                listOf(
                    FilterStatus("CAPE ≤ 8", capeOk),
                    FilterStatus("Цена ≥ SMA200 или RSI14 < 30", techOk),
                    FilterStatus("Золотой крест", crossOk),
                    FilterStatus("Подушка ≥ 3× месячный поток", cushionOk),
                ),
        )
    }

    fun passRiskFilters(
        marketData: MarketData,
        portfolio: Portfolio,
        config: StrategyConfig,
    ): Boolean = getFilterStatuses(marketData, portfolio, config).filterStatuses.all { it.passed }

    fun evaluate(
        date: LocalDate,
        marketData: MarketData,
        portfolio: Portfolio,
        config: StrategyConfig,
    ): List<Action> {
        val actions = mutableListOf<Action>()
        if (date.dayOfMonth == 10) actions.add(Action.Dca)
        val d = delta(marketData)
        val tranche = enhancedTranche(d, marketData.sigma30, config)
        if (tranche > 0.0 && passRiskFilters(marketData, portfolio, config)) {
            actions.add(Action.Enhanced(tranche))
        }
        return actions
    }
}
