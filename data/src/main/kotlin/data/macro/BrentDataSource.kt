package data.macro

import okhttp3.OkHttpClient

class BrentDataSource(private val client: OkHttpClient) {
    suspend fun fetchBrentPrice(): Double {
        val url = "https://raw.githubusercontent.com/datasets/oil-prices/master/data/brent-daily.csv"
        val request = okhttp3.Request.Builder().url(url).build()
        client.newCall(request).execute().use { resp ->
            val body = resp.body?.string() ?: error("empty body")
            val line = body.trim().lineSequence().last()
            val price = line.substringAfter(',')
            return price.toDouble()
        }
    }
}
