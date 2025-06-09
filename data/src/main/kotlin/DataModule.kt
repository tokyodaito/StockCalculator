package data

import data.market.MoexDataSource
import data.market.MoexRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.time.Duration

val dataModule = module {
    single {
        OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(10))
            .build()
    }
    single { Json { ignoreUnknownKeys = true } }
    single { MoexRepository(get(), get()) }
    single { MoexDataSource(get()) }
    single<ChatConfigRepository> { InMemoryChatConfigRepository() }
}
