package data

import data.macro.BrentDataSource
import data.macro.KeyRateDataSource
import data.macro.MacroDataSource
import data.market.CapeRepository
import data.market.CapeRepositoryImpl
import data.market.MoexDataSource
import data.market.MoexRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.time.Duration

val dataModule =
    module {
        single {
            OkHttpClient
                .Builder()
                .callTimeout(Duration.ofSeconds(10))
                .build()
        }
        single { Json { ignoreUnknownKeys = true } }
        single { MoexRepository(get(), get()) }
        single<CapeRepository> { CapeRepositoryImpl(get(), get()) }
        single { MoexDataSource(get(), get()) }
        single { BrentDataSource(get()) }
        single { KeyRateDataSource(get()) }
        single { MacroDataSource(get(), get()) }
        single<ChatConfigRepository> { InMemoryChatConfigRepository() }
    }
