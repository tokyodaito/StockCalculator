package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration
import java.time.LocalDate

data class MarketData(
    val price: Double,
    val max52: Double,
    val sma200: Double,
    val rsi14: Double,
    val pe: Double,
    val dy: Double,
    val ofzYield: Double
)

object MoexDataSource {
    private val client = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(10))
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /** Асинхронный метод: возвращает MarketData */
    suspend fun fetchMarketData(): MarketData = withContext(Dispatchers.IO) {
        val baseUrl = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"
        val today   = LocalDate.now()
        val from    = today.minusDays(365)

        val urlTemplate = "$baseUrl/history/engines/stock/markets/index/securities/IMOEX.json".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("from",     from.toString())
            .addQueryParameter("till",     today.toString())
            .addQueryParameter("iss.meta", "off")
            .addQueryParameter("iss.only", "history,history.cursor")

        val closes = mutableListOf<Double>()
        val highs  = mutableListOf<Double>()
        var start   = 0
        var total: Int
        var pageSize: Int

        do {
            val url = urlTemplate
                .setQueryParameter("start", start.toString())
                .build()
            val root = makeRequest(url)

            val history   = root["history"]!!.jsonObject
            val columns   = history["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
            val closeIdx  = columns.indexOf("CLOSE")
                .takeIf { it >= 0 } ?: error("CLOSE не найден в history.columns")
            val highIdx   = columns.indexOf("HIGH")
                .takeIf { it >= 0 } ?: error("HIGH не найден в history.columns")

            history["data"]!!.jsonArray.forEach { row ->
                val arr = row.jsonArray
                closes += arr[closeIdx].jsonPrimitive.double
                highs  += arr[highIdx].jsonPrimitive.double
            }

            val cursorRow = root["history.cursor"]!!
                .jsonObject["data"]!!.jsonArray[0].jsonArray
            total    = cursorRow[1].jsonPrimitive.int
            pageSize = cursorRow[2].jsonPrimitive.int
            start   += pageSize
        } while (start < total)

        require(closes.size >= 200) {
            "Недостаточно данных: нужно ≥200 закрытий, получено ${closes.size}"
        }

        val price    = closes.last()
        val max52    = highs.maxOrNull()!!
        val sma200   = closes.takeLast(200).average()
        val rsi14    = calculateRsi14(closes)

        // Заглушки
        val pe       = 5.7
        val dy       = 7.5
        val ofzYield = 15.30

        MarketData(price, max52, sma200, rsi14, pe, dy, ofzYield)
    }

    private fun makeRequest(url: HttpUrl): JsonObject {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) {
                throw IllegalStateException("HTTP ${res.code}: ${res.message}")
            }
            return json.parseToJsonElement(res.body!!.string()).jsonObject
        }
    }

    // RSI14 по алгоритму Wilder
    private fun calculateRsi14(closes: List<Double>): Double {
        require(closes.size >= 15) {
            "RSI14 ожидает ≥15 значений, получено ${closes.size}"
        }
        val diffs   = closes.zipWithNext { prev, next -> next - prev }
        var avgGain = diffs.take(14).map { maxOf(it, 0.0) }.average()
        var avgLoss = diffs.take(14).map { maxOf(-it, 0.0) }.average()
        for (i in 14 until diffs.size) {
            val gain = maxOf(diffs[i], 0.0)
            val loss = maxOf(-diffs[i], 0.0)
            avgGain = (avgGain * 13 + gain) / 14
            avgLoss = (avgLoss * 13 + loss) / 14
        }
        val rs = if (avgLoss == 0.0) Double.POSITIVE_INFINITY else avgGain / avgLoss
        return 100 - 100 / (1 + rs)
    }
}
