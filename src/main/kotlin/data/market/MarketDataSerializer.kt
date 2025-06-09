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

    fun parseAnalytics(root: JsonObject): Pair<Double, Double> {
        val analytics = root["analytics"]!!.jsonObject
        val cols = analytics["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
        val idxPe = cols.indexOf("P/E").takeIf { it >= 0 } ?: error("P/E not found")
        val idxDy = cols.indexOf("YIELD").takeIf { it >= 0 } ?: cols.indexOf("DIVYIELD").takeIf { it >= 0 } ?: error("DY not found")
        val row = analytics["data"]!!.jsonArray[0].jsonArray
        val pe = row[idxPe].jsonPrimitive.double
        val dy = row[idxDy].jsonPrimitive.double
        return pe to dy
    }

    fun parseZcyc(root: JsonObject): Double {
        val zcyc = root["zcyc"]!!.jsonObject
        val cols = zcyc["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
        val idxMat = cols.indexOf("MATURITY").takeIf { it >= 0 } ?: error("MATURITY not found")
        val idxYld = cols.indexOf("YIELD").takeIf { it >= 0 } ?: error("YIELD not found")
        val targetYear = java.time.LocalDate.now().plusYears(10).year
        val row = zcyc["data"]!!.jsonArray
            .map { it.jsonArray }
            .first { java.time.LocalDate.parse(it[idxMat].jsonPrimitive.content).year == targetYear }
        return row[idxYld].jsonPrimitive.double
    }

    fun toMarketData(closes: List<Double>, highs: List<Double>, pe: Double, dy: Double, ofzYield: Double): MarketData {
        require(closes.size >= 200) { "Недостаточно данных: нужно ≥200 закрытий, получено ${closes.size}" }
        val price = closes.last()
        val max52 = highs.maxOrNull()!!
        val sma200 = closes.takeLast(200).average()
        val rsi14 = calculateRsi14(closes)
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

