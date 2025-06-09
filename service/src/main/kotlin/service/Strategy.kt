package org.example

import data.market.MarketData
import java.time.LocalDate

object Strategy {
    fun delta(m: MarketData): Double = (m.price - m.max52) / m.max52 * 100.0

    fun enhancedTranche(delta: Double, config: StrategyConfig): Double = when {
        delta <= -30.0 -> 2.0 * config.baseDcaAmount
        delta <= -20.0 -> 1.5 * config.baseDcaAmount
        delta <= -10.0 -> 1.0 * config.baseDcaAmount
        else -> 0.0
    }

    fun getFilterStatuses(m: MarketData, p: Portfolio, config: StrategyConfig): List<FilterStatus> {
        val techOk = m.price >= m.sma200 || m.rsi14 < 30.0
        val peOk = m.pe <= 6.6
        val dyOk = m.dy >= m.ofzYield + 2.0
        val cushionOk = p.cushionAmount / config.monthlyFlow >= config.minCushionRatio
        return listOf(
            FilterStatus("Технический (P ≥ SMA200 или RSI14 < 30)", techOk),
            FilterStatus("Оценка P/E ≤ 6,6×", peOk),
            FilterStatus("Дивдоходность ≥ OFZ-10 + 2 п.п.", dyOk),
            FilterStatus("Подушка ≥ ${config.minCushionRatio}× месячный поток", cushionOk),
        )
    }

    fun passRiskFilters(m: MarketData, p: Portfolio, config: StrategyConfig): Boolean =
        getFilterStatuses(m, p, config).all { it.passed }

    fun evaluate(date: LocalDate, m: MarketData, p: Portfolio, config: StrategyConfig): List<Action> {
        val actions = mutableListOf<Action>()
        if (date.dayOfMonth == 10) actions.add(Action.Dca)
        val d = delta(m)
        val tranche = enhancedTranche(d, config)
        if (tranche > 0.0 && passRiskFilters(m, p, config)) {
            actions.add(Action.Enhanced(tranche))
        }
        return actions
    }
}
