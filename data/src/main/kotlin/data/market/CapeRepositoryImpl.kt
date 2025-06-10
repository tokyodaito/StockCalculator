package data.market

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.nio.file.Files

private const val CapeFile = "IMOEX_FUNDAMENTALS.json"

class CapeRepositoryImpl(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: String = "https://capital-gain.ru",
) : CapeRepository {
    override suspend fun fetchCape(): Double =
        withContext(Dispatchers.IO) {
            val url = "$baseUrl/data/$CapeFile"
            val body =
                if (url.startsWith("file:")) {
                    val uri = URI(url)
                    Files.readString(
                        java.nio.file.Path
                            .of(uri),
                    )
                } else {
                    val request =
                        Request
                            .Builder()
                            .url(url)
                            .header("Accept", "application/json")
                            .build()
                    client.newCall(request).execute().use { it.body?.string() ?: error("empty body") }
                }
            val arr =
                try {
                    json.parseToJsonElement(body).jsonArray
                } catch (_: Exception) {
                    throw IllegalArgumentException("CAPE data not found")
                }
            val last =
                arr.lastOrNull { it.jsonObject["cape"] != null }
                    ?: throw IllegalArgumentException("CAPE data not found")
            last.jsonObject["cape"]!!.jsonPrimitive.double
        }
}
