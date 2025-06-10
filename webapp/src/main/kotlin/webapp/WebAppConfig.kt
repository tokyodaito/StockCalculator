package webapp

import data.macro.BrentDataSource
import data.macro.KeyRateDataSource
import data.macro.MacroDataSource
import data.market.CapeRepository
import data.market.CapeRepositoryImpl
import data.market.MoexDataSource
import data.market.MoexRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import service.DcaService
import java.time.Duration

@Configuration
class WebAppConfig {
    @Bean
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(10))
        .build()

    @Bean
    fun json(): Json = Json { ignoreUnknownKeys = true }

    @Bean
    fun moexRepository(client: OkHttpClient, json: Json) = MoexRepository(client, json)

    @Bean
    fun capeRepository(client: OkHttpClient, json: Json): CapeRepository = CapeRepositoryImpl(client, json)

    @Bean
    fun moexDataSource(repo: MoexRepository, capeRepository: CapeRepository) = MoexDataSource(repo, capeRepository)

    @Bean
    fun brentSource(client: OkHttpClient) = BrentDataSource(client)

    @Bean
    fun keyRateSource(client: OkHttpClient) = KeyRateDataSource(client)

    @Bean
    fun macroDataSource(brentSource: BrentDataSource, keyRateSource: KeyRateDataSource) =
        MacroDataSource(brentSource, keyRateSource)

    @Bean
    fun dcaService(market: MoexDataSource, macro: MacroDataSource) =
        DcaService({ market.fetchMarketData() }, { date -> macro.fetchMacroData(date) })
}
