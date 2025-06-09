package data.market

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.LocalDate

class MoexRepository(
    client: OkHttpClient,
    json: Json,
) {
    private val service: MoexService
    private val baseUrl: String = System.getProperty("moex.base") ?: "https://iss.moex.com/iss"

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        service = retrofit.create(MoexService::class.java)
    }

    internal suspend fun fetchPage(from: LocalDate, till: LocalDate, start: Int): MarketPageResponse =
        service.history(from.toString(), till.toString(), start)
}

private interface MoexService {
    @GET("history/engines/stock/markets/index/securities/IMOEX.json")
    suspend fun history(
        @Query("from") from: String,
        @Query("till") till: String,
        @Query("start") start: Int,
        @Query("iss.meta") meta: String = "off",
        @Query("iss.only") only: String = "history,history.cursor",
    ): MarketPageResponse
}
