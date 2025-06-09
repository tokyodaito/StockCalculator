package org.example

sealed class Action {
    object Dca : Action()
    data class Enhanced(val amount: Double) : Action()
}
