package org.example

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.example.di.AppModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MoexClientTest {
    @Test
    fun `parse market data`() = runBlocking {
        val server  = MockWebServer()
        val history = javaClass.getResource("/history.json")!!.readText()
        val analytics = javaClass.getResource("/analytics.json")!!.readText()
        val zcyc = javaClass.getResource("/zcyc.json")!!.readText()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path!!.contains("/history/engines/stock/markets/index/securities/IMOEX.json") -> MockResponse().setBody(history)
                    request.path!!.contains("/engines/stock/markets/index/securities/IMOEX.json") -> MockResponse().setBody(analytics)
                    request.path!!.contains("/engines/stock/markets/zcyc.json") -> MockResponse().setBody(zcyc)
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        val data = AppModule.dataSource.fetchMarketData()

        server.shutdown()
        assertEquals(2786.16, data.price)
        assertEquals(3371.06, data.max52)
        assertEquals(5.7, data.pe)
        assertEquals(7.5, data.dy)
        assertEquals(15.30, data.ofzYield)
    }
}
