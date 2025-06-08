package org.example

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MoexClientTest {
    @Test
    fun `parse market data`() {
        val server = MockWebServer()
        val history = this::class.java.getResource("/history.json")!!.readText()
        server.enqueue(MockResponse().setBody(history))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))
        val data = MoexClient.fetchMarketData()
        server.shutdown()
        assertEquals(2786.16, data.price)
        assertEquals(3326.14, data.max52)
    }
}
