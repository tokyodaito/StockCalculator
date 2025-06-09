package data.macro

import java.time.LocalDate

class MacroDataSource(
    private val brent: BrentDataSource,
    private val key: KeyRateDataSource,
) {
    suspend fun fetchMacroData(date: LocalDate): MacroData {
        val br = brent.fetchBrentPrice()
        val (keyNow, keyPast) = key.fetchKeyRates(date)
        return MacroData(br, keyNow, keyPast)
    }
}
