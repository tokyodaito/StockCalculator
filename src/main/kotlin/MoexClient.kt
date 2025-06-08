package org.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate

object MoexClient {
    private val client = OkHttpClient()
    private val json = Json

    fun fetchMarketData(base: String = "https://iss.moex.com/iss"): MarketData {
        val infoUrl = "$base/engines/stock/markets/index/boards/SNDX/securities/IMOEX.json?iss.meta=off&iss.only=securities,marketdata"
        val infoText = request(infoUrl)
        val infoJson = json.parseToJsonElement(infoText).jsonObject
        val sec = infoJson["securities"]!!.jsonObject["data"]!!.jsonArray[0].jsonArray
        val md = infoJson["marketdata"]!!.jsonObject["data"]!!.jsonArray[0].jsonArray
        val price = md[2].toString().toDouble()
        val high52 = sec[5].toString().toDouble()

        val today = LocalDate.now()
        val from = today.minusDays(250)
        val histUrl = "$base/history/engines/stock/markets/index/securities/IMOEX.json?from=$from&till=$today&iss.meta=off&iss.only=history"
        val histText = request(histUrl)
        val histJson = json.parseToJsonElement(histText).jsonObject
        val closes = histJson["history"]!!.jsonObject["data"]!!.jsonArray.map {
            it.jsonArray[5].toString().toDouble()
        }
        val sma = closes.takeLast(200).average()
        val rsi = rsi14(closes.takeLast(15))

        return MarketData(
            price = price,
            max52 = high52,
            sma200 = sma,
            rsi14 = rsi,
            pe = 6.3,
            dy = 13.0,
            ofzYield = 10.5
        )
    }

    private fun request(url: String): String {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { res ->
            return res.body!!.string()
        }
    }

    fun rsi14(closes: List<Double>): Double {
        val diffs = closes.zipWithNext { a, b -> b - a }
        val gains = diffs.map { if (it > 0) it else 0.0 }
        val losses = diffs.map { if (it < 0) -it else 0.0 }
        val avgGain = gains.average()
        val avgLoss = losses.average()
        val rs = if (avgLoss == 0.0) Double.POSITIVE_INFINITY else avgGain / avgLoss
        return 100 - 100 / (1 + rs)
    }
}
