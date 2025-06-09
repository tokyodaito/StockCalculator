package data.market

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpCapeRepository(
    private val client: OkHttpClient,
) : CapeRepository {
    override suspend fun fetchCape(): Double {
        val request = Request.Builder()
            .url("https://fred.stlouisfed.org/graph/fredgraph.csv?id=RUCAPE")
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: error("empty body")
                val lines = body.lineSequence()
                    .filter { it.contains(",") && !it.startsWith("observation_date") }
                    .toList()
                val last = lines.last()
                last.substringAfter(',').toDouble()
            }
        }
    }
}
