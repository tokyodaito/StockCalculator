package org.example

import data.market.CapeRepository
import data.market.MoexDataSource
import data.market.MoexRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MoexClientTest {
    @Test
    fun `parse market data`() = runBlocking {
        val server  = MockWebServer()
        val history = javaClass.getResource("/history.json")!!.readText()
        server.enqueue(MockResponse().setBody(history))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        val client = OkHttpClient()
        val repo = MoexRepository(client, Json { ignoreUnknownKeys = true })
        val capeRepo = object : CapeRepository {
            override suspend fun fetchCape(): Double = 7.0
        }
        val dataSource = MoexDataSource(repo, capeRepo)
        val data = dataSource.fetchMarketData()

        server.shutdown()
        assertEquals(2786.16, data.price, 0.01)
        assertEquals(3371.06, data.max52, 0.01)
        assertEquals(7.0, data.cape, 0.001)
    }
}
