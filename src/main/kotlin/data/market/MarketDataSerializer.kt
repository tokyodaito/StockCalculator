package data.market

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int

internal object MarketDataSerializer {

    fun parsePage(root: JsonObject): Page {
        val history = root["history"]!!.jsonObject
        val columns = history["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
        val closeIdx = columns.indexOf("CLOSE").takeIf { it >= 0 } ?: error("CLOSE не найден")
        val highIdx = columns.indexOf("HIGH").takeIf { it >= 0 } ?: error("HIGH не найден")

        val closes = history["data"]!!.jsonArray.map { it.jsonArray[closeIdx].jsonPrimitive.double }
        val highs = history["data"]!!.jsonArray.map { it.jsonArray[highIdx].jsonPrimitive.double }

        val cursorRow = root["history.cursor"]!!.jsonObject["data"]!!.jsonArray[0].jsonArray
        val total = cursorRow[1].jsonPrimitive.int
        val pageSize = cursorRow[2].jsonPrimitive.int

        return Page(closes, highs, total, pageSize)
    }

    fun toMarketData(closes: List<Double>, highs: List<Double>): MarketData {
        require(closes.size >= 200) { "Недостаточно данных: нужно ≥200 закрытий, получено ${closes.size}" }
        val price = closes.last()
        val max52 = highs.maxOrNull()!!
        val sma200 = closes.takeLast(200).average()
        val rsi14 = calculateRsi14(closes)
        val pe = 5.7
        val dy = 7.5
        val ofzYield = 15.30
        return MarketData(price, max52, sma200, rsi14, pe, dy, ofzYield)
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
}

internal data class Page(val closes: List<Double>, val highs: List<Double>, val total: Int, val pageSize: Int)

