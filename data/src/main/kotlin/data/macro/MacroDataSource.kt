package data.macro

import java.time.LocalDate

class MacroDataSource(
    private val brentSource: BrentDataSource,
    private val key: KeyRateDataSource,
) {
    suspend fun fetchMacroData(date: LocalDate): MacroData {
        val brent = brentSource.fetchBrentPrice()
        val keyRates = key.fetchKeyRates(date)
        return MacroData(brent = brent, keyRate = keyRates.current, keyRate6mAgo = keyRates.old)
    }
}
