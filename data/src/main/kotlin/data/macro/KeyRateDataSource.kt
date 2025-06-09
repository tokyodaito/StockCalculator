package data.macro

import okhttp3.OkHttpClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class KeyRateDataSource(private val client: OkHttpClient) {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    suspend fun fetchKeyRates(date: LocalDate): Pair<Double, Double> {
        val from = date.minusMonths(6)
        val url =
            "https://www.cbr.ru/eng/hd_base/KeyRate/?UniDbQuery.Posted=True&" +
                "UniDbQuery.From=${from.format(formatter)}&" +
                "UniDbQuery.To=${date.format(formatter)}"
        val request = okhttp3.Request.Builder().url(url).build()
        client.newCall(request).execute().use { resp ->
            val body = resp.body?.string() ?: error("empty body")
            val regex = Regex("<td>(\\d{2}\\.\\d{2}\\.\\d{4})</td>\\s*<td>(\\d+(?:\\.\\d+)?)</td>")
            val matches = regex.findAll(body).toList()
            if (matches.isEmpty()) error("rates not found")
            val current = matches.first().groupValues[2].replace(',', '.').toDouble()
            val targetDate = from.format(formatter)
            val old =
                matches.find { it.groupValues[1] == targetDate }?.groupValues?.get(2)
                    ?.replace(',', '.')?.toDouble() ?: current
            return current to old
        }
    }
}
