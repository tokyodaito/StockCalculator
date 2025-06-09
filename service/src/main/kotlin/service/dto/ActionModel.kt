package service.dto

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
