package data.market

data class MarketData(
    val price: Double,
    val max52: Double,
    val sma200: Double,
    val rsi14: Double,
    val pe: Double,
    val dy: Double,
    val ofzYield: Double
)
