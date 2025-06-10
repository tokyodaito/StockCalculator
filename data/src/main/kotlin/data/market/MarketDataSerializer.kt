package data.market

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.ln
import kotlin.math.sqrt

internal object MarketDataSerializer {
    fun parsePage(root: MarketPageResponse): Page {
        val columns = root.history.columns
        val closeIdx = columns.indexOf("CLOSE").takeIf { it >= 0 } ?: error("CLOSE не найден")
        val highIdx = columns.indexOf("HIGH").takeIf { it >= 0 } ?: error("HIGH не найден")

        val closes = root.history.data.map { it[closeIdx].jsonPrimitive.double }
        val highs = root.history.data.map { it[highIdx].jsonPrimitive.double }

        val cursorRow = root.cursor.data.first()
        val total = cursorRow[1]
        val pageSize = cursorRow[2]

        return Page(closes, highs, total, pageSize)
    }

    fun toMarketData(
        closes: List<Double>,
        highs: List<Double>,
        cape: Double,
    ): MarketData {
        require(closes.size >= 200) { "Недостаточно данных: нужно ≥200 закрытий, получено ${closes.size}" }
        val price = closes.last()
        val max52 = highs.maxOrNull()!!
        val sma200 = closes.takeLast(200).average()
        val sma50 = closes.takeLast(50).average()
        val rsi14 = calculateRsi14(closes)
        val sigma30 = calculateSigma30(closes)
        return MarketData(
            price = price,
            max52 = max52,
            sma200 = sma200,
            sma50 = sma50,
            rsi14 = rsi14,
            sigma30 = sigma30,
            cape = cape,
        )
    }

    private fun calculateRsi14(closes: List<Double>): Double {
        require(closes.size >= 15)
        val diffs = closes.zipWithNext { a, b -> b - a }
        var gain = diffs.take(14).map { maxOf(it, 0.0) }.average()
        var loss = diffs.take(14).map { maxOf(-it, 0.0) }.average()
        for (i in 14 until diffs.size) {
            val g = maxOf(diffs[i], 0.0)
            val l = maxOf(-diffs[i], 0.0)
            gain = (gain * 13 + g) / 14
            loss = (loss * 13 + l) / 14
        }
        val rs = if (loss == 0.0) Double.POSITIVE_INFINITY else gain / loss
        return 100 - 100 / (1 + rs)
    }

    private fun calculateSigma30(closes: List<Double>): Double {
        require(closes.size >= 31)
        val returns = closes.takeLast(31).zipWithNext { a, b -> ln(b / a) }
        val mean = returns.average()
        val variance = returns.sumOf { (it - mean) * (it - mean) } / (returns.size - 1)
        return sqrt(variance) * sqrt(252.0)
    }
}

internal data class Page(
    val closes: List<Double>,
    val highs: List<Double>,
    val total: Int,
    val pageSize: Int,
)

@Serializable
internal data class MarketPageResponse(
    val history: DataSet,
    @SerialName("history.cursor") val cursor: CursorSet,
)

@Serializable
internal data class DataSet(
    val columns: List<String>,
    val data: List<List<JsonElement>>,
)

@Serializable
internal data class CursorSet(
    val data: List<List<Int>>,
)
