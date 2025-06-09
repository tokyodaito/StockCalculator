package data.market

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpCapeRepository(
    private val client: OkHttpClient,
    private val url: String = System.getProperty("cape.url")
        ?: "https://fred.stlouisfed.org/graph/fredgraph.csv?id=RUCAPE",
) : CapeRepository {
    override suspend fun fetchCape(): Double {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/csv")
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: error("empty body")
                val lines = body.lineSequence()
                    .filter { it.matches(Regex("\\d{4}-\\d{2}-\\d{2},.*")) }
                    .toList()
                require(lines.isNotEmpty()) { "CAPE data not found" }
                return@use lines.last().substringAfter(',').toDouble()
            }
        }
    }
}
