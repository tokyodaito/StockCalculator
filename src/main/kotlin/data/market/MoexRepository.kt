package data.market

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration
import java.time.LocalDate

class MoexRepository(
    private val client: OkHttpClient,
    private val json: Json,
) {
    private val baseUrl: String = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"

    suspend fun fetchPage(from: LocalDate, till: LocalDate, start: Int): JsonObject = withContext(Dispatchers.IO) {
        val url = "$baseUrl/history/engines/stock/markets/index/securities/IMOEX.json".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("from", from.toString())
            .addQueryParameter("till", till.toString())
            .addQueryParameter("iss.meta", "off")
            .addQueryParameter("iss.only", "history,history.cursor")
            .addQueryParameter("start", start.toString())
            .build()
        makeRequest(url)
    }

    suspend fun fetchAnalytics(): JsonObject = withContext(Dispatchers.IO) {
        val url = "$baseUrl/engines/stock/markets/index/securities/IMOEX.json".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("iss.meta", "off")
            .addQueryParameter("iss.only", "analytics")
            .build()
        makeRequest(url)
    }

    suspend fun fetchZcyc(from: LocalDate, till: LocalDate): JsonObject = withContext(Dispatchers.IO) {
        val url = "$baseUrl/engines/stock/markets/zcyc.json".toHttpUrlOrNull()!!
            .newBuilder()
            .addQueryParameter("iss.meta", "off")
            .addQueryParameter("iss.only", "zcyc")
            .addQueryParameter("from", from.toString())
            .addQueryParameter("till", till.toString())
            .build()
        makeRequest(url)
    }

    private fun makeRequest(url: HttpUrl): JsonObject {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) {
                throw IllegalStateException("HTTP ${res.code}: ${res.message}")
            }
            val responseBody = res.body ?: throw IllegalStateException("Response body is null")
            return json.parseToJsonElement(responseBody.string()).jsonObject
        }
    }
}
