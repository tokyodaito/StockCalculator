package service.dto

import data.market.MarketData

internal sealed class Action {
    object Dca : Action()

    data class Enhanced(
        val amount: Double,
    ) : Action()
}

internal data class FilterStatus(
    val name: String,
    val passed: Boolean,
)

internal data class Filters(
    val marketData: MarketData,
    val filterStatuses: List<FilterStatus>,
)
