package org.example.di

import data.market.MoexDataSource
import data.market.MoexRepository
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.time.Duration

object AppModule {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(10))
            .build()
    }

    private val json: Json by lazy { Json { ignoreUnknownKeys = true } }

    private val repository by lazy { MoexRepository(client, json) }
    val dataSource by lazy { MoexDataSource(repository) }
}