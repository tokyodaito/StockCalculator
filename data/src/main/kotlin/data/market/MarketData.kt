package data.market

data class MarketData(
    val price: Double,
    val max52: Double,
    val sma200: Double,
    val sma50: Double,
    val rsi14: Double,
    val sigma30: Double,
    val cape: Double,
)
