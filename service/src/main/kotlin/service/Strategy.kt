package service

import data.market.MarketData
import service.dto.Action
import service.dto.FilterStatus
import service.dto.Portfolio
import service.dto.StrategyConfig
import java.time.LocalDate

internal object Strategy {
    fun delta(m: MarketData): Double = (m.price - m.max52) / m.max52 * 100.0

    fun enhancedTranche(
        delta: Double,
        sigma30: Double,
        config: StrategyConfig,
    ): Double {
        val k = when {
            delta <= -20.0 -> 2.0
            delta <= -10.0 -> 1.0
            else -> 0.0
        }
        if (k == 0.0) return 0.0
        val adjusted = (k * (0.10 / sigma30)).coerceIn(0.4, 2.5)
        return adjusted * config.baseDcaAmount
    }

    fun getFilterStatuses(
        m: MarketData,
        p: Portfolio,
        config: StrategyConfig,
    ): List<FilterStatus> {
        val capeOk = m.cape <= 8.0
        val techOk = m.price >= m.sma200 || m.rsi14 < 30.0
        val crossOk = m.sma50 > m.sma200
        val cushionOk = p.cushionAmount / config.monthlyFlow >= 3.0
        return listOf(
            FilterStatus("CAPE ≤ 8", capeOk),
            FilterStatus("Цена ≥ SMA200 или RSI14 < 30", techOk),
            FilterStatus("Золотой крест", crossOk),
            FilterStatus("Подушка ≥ 3× месячный поток", cushionOk),
        )
    }

    fun passRiskFilters(
        m: MarketData,
        p: Portfolio,
        config: StrategyConfig,
    ): Boolean = getFilterStatuses(m, p, config).all { it.passed }

    fun evaluate(
        date: LocalDate,
        m: MarketData,
        p: Portfolio,
        config: StrategyConfig,
    ): List<Action> {
        val actions = mutableListOf<Action>()
        if (date.dayOfMonth == 10) actions.add(Action.Dca)
        val d = delta(m)
        val tranche = enhancedTranche(d, m.sigma30, config)
        if (tranche > 0.0 && passRiskFilters(m, p, config)) {
            actions.add(Action.Enhanced(tranche))
        }
        return actions
    }
}
