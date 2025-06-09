package org.example

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.toString

class MoexClientTest {
    @Test
    fun `parse market data`() = runBlocking {
        val server  = MockWebServer()
        val history = javaClass.getResource("/history.json")!!.readText()
        server.enqueue(MockResponse().setBody(history))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        val data = MoexDataSource.fetchMarketData()

        server.shutdown()
        assertEquals(2786.16, data.price)
        assertEquals(3371.06, data.max52)
    }
}
