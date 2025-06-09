package data.market

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.nio.file.Files

class HttpCapeRepository(
    private val client: OkHttpClient,
    private val url: String = System.getProperty("cape.url")
        ?: "https://fred.stlouisfed.org/graph/fredgraph.csv?id=RUCAPE",
) : CapeRepository {
    override suspend fun fetchCape(): Double {
        return withContext(Dispatchers.IO) {
            val body = if (url.startsWith("file:")) {
                val uri = URI(url)
                Files.readString(java.nio.file.Path.of(uri))
            } else {
                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "text/csv")
                    .build()
                client.newCall(request).execute().use { it.body?.string() ?: error("empty body") }
            }
            val lines = body.lineSequence()
                .filter { it.matches(Regex("\\d{4}-\\d{2}-\\d{2},.*")) }
                .toList()
            require(lines.isNotEmpty()) { "CAPE data not found" }
            lines.last().substringAfter(',').toDouble()
        }
    }
}
