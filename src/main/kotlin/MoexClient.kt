package org.example

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate

// Модель рыночных данных
data class MarketData(
    val price: Double,    // текущее закрытие
    val max52: Double,    // максимум за ≈52 недели
    val sma200: Double,   // 200-дневная SMA
    val rsi14: Double,    // RSI(14)
    val pe: Double,       // P/E (заглушка)
    val dy: Double,       // дивидендная доходность (заглушка)
    val ofzYield: Double  // доходность ОФЗ-10 (заглушка)
)

object MoexClient {
    private val client = OkHttpClient()
    private val json   = Json { ignoreUnknownKeys = true }

    fun fetchMarketData(): MarketData {
        val base  = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"
        val today = LocalDate.now()
        // Запрашиваем 365 календарных дней, чтобы гарантированно получить ≥200 торговых дней
        val from  = today.minusDays(365)

        // Берём сразу обе таблицы: history и history.cursor
        val histUrl = "$base/history/engines/stock/markets/index/" +
                "securities/IMOEX.json" +
                "?from=$from&till=$today" +
                "&iss.meta=off" +
                "&iss.only=history,history.cursor"

        // Первый запрос
        val firstText = request(histUrl)
        val firstJson = json.parseToJsonElement(firstText).jsonObject

        // Парсим историю и cursor
        val historyObj = firstJson["history"]!!.jsonObject
        val histCols   = historyObj["columns"]!!.jsonArray.map { it.jsonPrimitive.content }
        val closeIdx   = histCols.indexOf("CLOSE").takeIf { it >= 0 }
            ?: throw IllegalStateException("Не найдено поле CLOSE в history")
        val pageData   = historyObj["data"]!!.jsonArray

        val closes = mutableListOf<Double>()
        closes += pageData.map { it.jsonArray[closeIdx].jsonPrimitive.double }

        val cursorObj = firstJson["history.cursor"]!!.jsonObject
        val cursorRow = cursorObj["data"]!!.jsonArray[0].jsonArray
        val total     = cursorRow[1].jsonPrimitive.int
        val pageSize  = cursorRow[2].jsonPrimitive.int

        // Догружаем остальные страницы
        var start = pageSize
        while (start < total) {
            val pageText = request("$histUrl&start=$start")
            val pageJson = json.parseToJsonElement(pageText).jsonObject
            val pageHist = pageJson["history"]!!.jsonObject["data"]!!.jsonArray
            closes += pageHist.map { it.jsonArray[closeIdx].jsonPrimitive.double }
            start += pageSize
        }

        // Жёсткие проверки
        require(closes.size >= 200) {
            "Недостаточно данных: нужно ≥200 закрытий, получено ${closes.size}"
        }
        require(closes.size >= 15)  {
            "Недостаточно данных: нужно ≥15 точек для RSI14, получено ${closes.size}"
        }

        // Расчёт метрик
        val price  = closes.last()
        val max52  = closes.maxOrNull()
            ?: throw IllegalStateException("Не удалось вычислить max52")
        val sma200 = closes.takeLast(200).average()
        val rsi14  = calculateRsi14(closes.takeLast(15))

        // Заглушки
        val pe       = 6.3
        val dy       = 13.0
        val ofzYield = 10.5

        return MarketData(price, max52, sma200, rsi14, pe, dy, ofzYield)
    }

    private fun request(url: String): String {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ошибка ${res.code}")
            return res.body!!.string()
        }
    }

    // Жёсткий RSI14 по ровно 15 точкам
    private fun calculateRsi14(closes: List<Double>): Double {
        require(closes.size == 15) {
            "RSI14 ожидает ровно 15 точек, получено ${closes.size}"
        }
        val diffs   = closes.zipWithNext { prev, next -> next - prev }
        val gains   = diffs.map { if (it > 0) it else 0.0 }
        val losses  = diffs.map { if (it < 0) -it else 0.0 }
        val avgGain = gains.average()
        val avgLoss = losses.average()
        val rs      = if (avgLoss == 0.0) Double.POSITIVE_INFINITY else avgGain / avgLoss
        return 100.0 - 100.0 / (1 + rs)
    }
}
