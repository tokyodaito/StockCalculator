package org.example

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MoexClientTest {
    @Test
    fun `parse market data`() {
        val server = MockWebServer()
        val info = this::class.java.getResource("/info.json")!!.readText()
        val history = this::class.java.getResource("/history.json")!!.readText()
        server.enqueue(MockResponse().setBody(info))
        server.enqueue(MockResponse().setBody(history))
        server.start()
        val base = server.url("").toString().removeSuffix("/")
        val data = MoexClient.fetchMarketData(base)
        server.shutdown()
        assertEquals(2856.61, data.price)
        assertEquals(3371.06, data.max52)
    }
}
