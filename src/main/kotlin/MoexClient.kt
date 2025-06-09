package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

data class HistoryData(
    val price: Double,
    val max52: Double,
    val sma200: Double,
    val rsi14: Double
)

open class DataFetchException(msg: String) : RuntimeException(msg)
class PeNotFoundException(msg: String) : DataFetchException(msg)
class DividendsParseException(msg: String) : DataFetchException(msg)
class OfzYieldException(msg: String) : DataFetchException(msg)

object MoexDataSource {
    private val client = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(10))
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchMarketData(today: LocalDate = LocalDate.now()): MarketData = coroutineScope {
        val baseUrl = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"
        val from    = today.minusDays(365)

        val historyDeferred   = async { fetchHistory(baseUrl, from, today) }
        val analyticsDeferred = async { fetchAnalytics() }
        val ofzDeferred       = async { fetchOfzYield(today) }

        val history = historyDeferred.await()
        val (pe, dy) = analyticsDeferred.await()
        val ofz = ofzDeferred.await()

        MarketData(history.price, history.max52, history.sma200, history.rsi14, pe, dy, ofz)
    }

    private suspend fun fetchHistory(baseUrl: String, from: LocalDate, till: LocalDate): HistoryData = withContext(Dispatchers.IO) {
        val urlTemplate = "$baseUrl/history/engines/stock/markets/index/securities/IMOEX.json".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("from", from.toString())
            .addQueryParameter("till", till.toString())
            .addQueryParameter("iss.meta", "off")
            .addQueryParameter("iss.only", "history,history.cursor")

        val closes = mutableListOf<Double>()
        val highs  = mutableListOf<Double>()
        var start   = 0
        var total: Int
        var pageSize: Int

        do {
            val url = urlTemplate.setQueryParameter("start", start.toString()).build()
            val root = makeRequest(url)

            val history   = root["history"]!!.jsonObject
            val columns   = history["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
            val closeIdx  = columns.indexOf("CLOSE").takeIf { it >= 0 } ?: throw DataFetchException("CLOSE not found")
            val highIdx   = columns.indexOf("HIGH").takeIf { it >= 0 } ?: throw DataFetchException("HIGH not found")

            history["data"]!!.jsonArray.forEach { row ->
                val arr = row.jsonArray
                closes += arr[closeIdx].jsonPrimitive.double
                highs  += arr[highIdx].jsonPrimitive.double
            }

            val cursorRow = root["history.cursor"]!!.jsonObject["data"]!!.jsonArray[0].jsonArray
            total    = cursorRow[1].jsonPrimitive.int
            pageSize = cursorRow[2].jsonPrimitive.int
            start   += pageSize
        } while (start < total)

        require(closes.size >= 200) { "Need ≥200 closes, got ${closes.size}" }

        val price  = closes.last()
        val max52  = highs.maxOrNull()!!
        val sma200 = closes.takeLast(200).average()
        val rsi14  = calculateRsi14(closes)

        HistoryData(price, max52, sma200, rsi14)
    }

    internal suspend fun fetchAnalytics(): Pair<Double, Double> = withContext(Dispatchers.IO) {
        val baseUrl = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"
        val url = "$baseUrl/engines/stock/markets/index/securities/IMOEX.json?iss.meta=off&iss.only=analytics".toHttpUrlOrNull()!!
        val root = makeRequest(url)
        val ana = root["analytics"]!!.jsonObject
        val cols = ana["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
        val data = ana["data"]!!.jsonArray.first().jsonArray
        val peIdx = cols.indexOf("P/E").takeIf { it >= 0 } ?: throw PeNotFoundException("P/E not found")
        val yIdx = cols.indexOf("YIELD").takeIf { it >= 0 } ?: cols.indexOf("DIVYIELD").takeIf { it >= 0 } ?: throw DividendsParseException("YIELD not found")
        val pe = data[peIdx].jsonPrimitive.double
        val dy = data[yIdx].jsonPrimitive.double
        pe to dy
    }

    internal suspend fun fetchDividends(price: Double): Double = withContext(Dispatchers.IO) {
        val baseUrl = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"
        val url = "$baseUrl/securities/IMOEX/dividends.json?iss.meta=off&iss.only=dividends".toHttpUrlOrNull()!!
        val root = makeRequest(url)
        val div = root["dividends"]!!.jsonObject
        val cols = div["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
        val idx = cols.indexOf("DIVIDEND").takeIf { it >= 0 } ?: throw DividendsParseException("DIVIDEND not found")
        val sum = div["data"]!!.jsonArray.sumOf { it.jsonArray[idx].jsonPrimitive.double }
        sum / price * 100
    }

    internal suspend fun fetchOfzYield(today: LocalDate, baseUrl: String = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"): Double = withContext(Dispatchers.IO) {
        val from = today.minusYears(1)
        val url = "$baseUrl/engines/stock/markets/zcyc.json".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("iss.meta", "off")
            .addQueryParameter("iss.only", "zcyc")
            .addQueryParameter("from", from.toString())
            .addQueryParameter("till", today.toString())
            .build()
        val root = makeRequest(url)
        val z = root["zcyc"]!!.jsonObject
        val cols = z["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
        val matIdx = cols.indexOf("MATURITY").takeIf { it >= 0 } ?: throw OfzYieldException("MATURITY not found")
        val yIdx = cols.indexOf("YIELD").takeIf { it >= 0 } ?: throw OfzYieldException("YIELD not found")
        val targetYear = today.plusYears(10).year
        val row = z["data"]!!.jsonArray.map { it.jsonArray }.firstOrNull {
            LocalDate.parse(it[matIdx].jsonPrimitive.content).year == targetYear
        } ?: throw OfzYieldException("10Y record not found")
        row[yIdx].jsonPrimitive.double
    }

    private fun makeRequest(url: HttpUrl): JsonObject {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) {
                throw DataFetchException("HTTP ${res.code}: ${res.message}")
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
