package org.example

import data.market.MarketData
import kotlinx.coroutines.runBlocking
import org.example.di.AppModule
import java.time.LocalDate

data class StrategyConfig(
    val monthlyFlow: Double = 100_000.0,
    val baseDcaAmount: Double = 50_000.0,
    val minCushionRatio: Double = 3.0
)

// Портфель пользователя
data class Portfolio(
    var equity: Double,       // сумма в акциях
    var others: Double,       // прочие активы
    var cushionAmount: Double // «подушка» в рублях
)

// Статус одного фильтра
data class FilterStatus(val name: String, val passed: Boolean)

// Возможные действия
sealed class Action {
    object Dca : Action()                             // базовая покупка
    data class Enhanced(val amount: Double) : Action()// усиленный транш
}

// Логика стратегии без ребаланса
object Strategy {

    // Δ = (price – max52) / max52 × 100%
    fun delta(m: MarketData): Double =
        (m.price - m.max52) / m.max52 * 100.0

    // k × BASE_DCA_AMOUNT в зависимости от Δ
    fun enhancedTranche(delta: Double, config: StrategyConfig): Double = when {
        delta <= -30.0 -> 2.0 * config.baseDcaAmount
        delta <= -20.0 -> 1.5 * config.baseDcaAmount
        delta <= -10.0 -> 1.0 * config.baseDcaAmount
        else           -> 0.0
    }

    // Генерация статусов четырех базовых фильтров
    fun getFilterStatuses(m: MarketData, p: Portfolio, config: StrategyConfig): List<FilterStatus> {
        val techOk    = m.price >= m.sma200 || m.rsi14 < 30.0
        val peOk      = m.pe <= 6.6
        val dyOk      = m.dy >= m.ofzYield + 2.0
        val cushionOk = p.cushionAmount / config.monthlyFlow >= config.minCushionRatio

        return listOf(
            FilterStatus("Технический (P ≥ SMA200 или RSI14 < 30)", techOk),
            FilterStatus("Оценка P/E ≤ 6,6×", peOk),
            FilterStatus("Дивдоходность ≥ OFZ-10 + 2 п.п.", dyOk),
            FilterStatus("Подушка ≥ ${config.minCushionRatio}× месячный поток", cushionOk)
        )
    }

    // Проверка, что все фильтры пройдены
    fun passRiskFilters(m: MarketData, p: Portfolio, config: StrategyConfig): Boolean =
        getFilterStatuses(m, p, config).all { it.passed }

    // Возвращает список действий на сегодня
    fun evaluate(date: LocalDate, m: MarketData, p: Portfolio, config: StrategyConfig): List<Action> {
        val actions = mutableListOf<Action>()

        // Фаза 1: базовый DCA 10-го числа
        if (date.dayOfMonth == 10) {
            actions.add(Action.Dca)
        }

        // Фазы 2+3: усиленный транш при просадке и прохождении фильтров
        val d = delta(m)
        val tranche = enhancedTranche(d, config)
        if (tranche > 0.0 && passRiskFilters(m, p, config)) {
            actions.add(Action.Enhanced(tranche))
        }

        return actions
    }
}

// Пример использования:
fun main() {
    runBlocking {
        val config = StrategyConfig()
        val market = AppModule.dataSource.fetchMarketData()
        val portfolio = Portfolio(
            equity = 700_000.0,
            others = 300_000.0,
            cushionAmount = 300_000.0
        )
        val today = LocalDate.of(2025, 6, 10)

        println("Статусы базовых фильтров риска:")
        Strategy.getFilterStatuses(market, portfolio, config).forEach { status ->
            println("${status.name}: ${if (status.passed) "✔" else "✘"}")
        }

        val actions = Strategy.evaluate(today, market, portfolio, config)
        if (actions.isEmpty()) {
            println("Покупки не требуются")
        } else {
            for (action in actions) {
                when (action) {
                    is Action.Dca ->
                        println("Выполнить базовый DCA: ${config.baseDcaAmount} ₽")

                    is Action.Enhanced ->
                        println("Усиленный транш: ${action.amount} ₽")
                }
            }
        }
    }
}
