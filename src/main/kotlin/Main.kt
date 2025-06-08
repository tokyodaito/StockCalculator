package org.example

import java.time.LocalDate

// Фаза 0: Настройки стратегии (без ребаланса и плеча)
object StrategyConfig {
    const val MONTHLY_FLOW          = 100_000.0    // ежемесячный приток ₽
    const val BASE_DCA_AMOUNT       = 50_000.0     // базовая сумма DCA (модель 50/50)
    const val MIN_CUSHION_RATIO     = 3.0          // минимум: подушка ≥ 3× месячный поток
}

// Данные рынка на текущую дату
data class MarketData(
    val price: Double,    // текущая цена IMOEX
    val max52: Double,    // максимум за 52 дня
    val sma200: Double,   // 200-дневная SMA
    val rsi14: Double,    // RSI(14)
    val pe: Double,       // P/E рынка
    val dy: Double,       // дивидендная доходность рынка, %
    val ofzYield: Double  // доходность ОФЗ-10, %
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
    fun enhancedTranche(delta: Double): Double = when {
        delta <= -30.0 -> 2.0 * StrategyConfig.BASE_DCA_AMOUNT
        delta <= -20.0 -> 1.5 * StrategyConfig.BASE_DCA_AMOUNT
        delta <= -10.0 -> 1.0 * StrategyConfig.BASE_DCA_AMOUNT
        else           -> 0.0
    }

    // Генерация статусов четырех базовых фильтров
    fun getFilterStatuses(m: MarketData, p: Portfolio): List<FilterStatus> {
        val techOk    = m.price >= m.sma200 || m.rsi14 < 30.0
        val peOk      = m.pe <= 6.6
        val dyOk      = m.dy >= m.ofzYield + 2.0
        val cushionOk = p.cushionAmount / StrategyConfig.MONTHLY_FLOW >= StrategyConfig.MIN_CUSHION_RATIO

        return listOf(
            FilterStatus("Технический (P ≥ SMA200 или RSI14 < 30)", techOk),
            FilterStatus("Оценка P/E ≤ 6,6×", peOk),
            FilterStatus("Дивдоходность ≥ OFZ-10 + 2 п.п.", dyOk),
            FilterStatus("Подушка ≥ 3× месячный поток", cushionOk)
        )
    }

    // Проверка, что все фильтры пройдены
    fun passRiskFilters(m: MarketData, p: Portfolio): Boolean =
        getFilterStatuses(m, p).all { it.passed }

    // Возвращает список действий на сегодня
    fun evaluate(date: LocalDate, m: MarketData, p: Portfolio): List<Action> {
        val actions = mutableListOf<Action>()

        // Фаза 1: базовый DCA 10-го числа
        if (date.dayOfMonth == 10) {
            actions.add(Action.Dca)
        }

        // Фазы 2+3: усиленный транш при просадке и прохождении фильтров
        val d = delta(m)
        val tranche = enhancedTranche(d)
        if (tranche > 0.0 && passRiskFilters(m, p)) {
            actions.add(Action.Enhanced(tranche))
        }

        return actions
    }
}

// Пример использования:
fun main() {
    val market = MarketData(
        price    = 2650.0,
        max52    = 3000.0,
        sma200   = 2700.0,
        rsi14    = 28.0,
        pe       = 6.3,
        dy       = 13.0,
        ofzYield = 10.5
    )
    val portfolio = Portfolio(
        equity        = 700_000.0,
        others        = 300_000.0,
        cushionAmount = 300_000.0  // три месячных потока
    )
    val today = LocalDate.of(2025, 6, 10)

    // Отобразить статусы фильтров
    println("Статусы базовых фильтров риска:")
    Strategy.getFilterStatuses(market, portfolio).forEach { status ->
        println("${status.name}: ${if (status.passed) "✔" else "✘"}")
    }

    // Оценить и выполнить действия
    val actions = Strategy.evaluate(today, market, portfolio)
    if (actions.isEmpty()) {
        println("Покупки не требуются")
    } else {
        for (action in actions) {
            when (action) {
                is Action.Dca       ->
                    println("Выполнить базовый DCA: ${StrategyConfig.BASE_DCA_AMOUNT} ₽")
                is Action.Enhanced  ->
                    println("Усиленный транш: ${action.amount} ₽")
            }
        }
    }
}
