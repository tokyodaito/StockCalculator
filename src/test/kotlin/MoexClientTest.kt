package org.example

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoexClientTest {
    @Test
    fun `parse market data`() = runBlocking {
        val server   = MockWebServer()
        val history  = javaClass.getResource("/history.json")!!.readText()
        val analytics = javaClass.getResource("/analytics.json")!!.readText()
        val zcyc     = javaClass.getResource("/zcyc.json")!!.readText()
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest): MockResponse {
                return when {
                    request.path!!.contains("history") -> MockResponse().setBody(history)
                    request.path!!.contains("analytics") -> MockResponse().setBody(analytics)
                    request.path!!.contains("zcyc") -> MockResponse().setBody(zcyc)
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        val today = LocalDate.of(2025, 6, 6)
        val data = MoexDataSource.fetchMarketData(today)

        server.shutdown()
        assertEquals(2786.16, data.price)
        assertEquals(3371.06, data.max52)
        assertEquals(5.7, data.pe)
        assertEquals(7.5, data.dy)
        assertEquals(15.3, data.ofzYield)
    }

    @Test
    fun `fetch analytics`() = runBlocking {
        val server = MockWebServer()
        val body = javaClass.getResource("/analytics.json")!!.readText()
        server.enqueue(MockResponse().setBody(body))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        val (pe, dy) = MoexDataSource.fetchAnalytics()

        server.shutdown()
        assertEquals(5.7, pe)
        assertEquals(7.5, dy)
    }

    @Test
    fun `fetch analytics missing field`() = runBlocking {
        val server = MockWebServer()
        val body = javaClass.getResource("/analytics_missing.json")!!.readText()
        server.enqueue(MockResponse().setBody(body))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        assertFailsWith<PeNotFoundException> { MoexDataSource.fetchAnalytics() }

        server.shutdown()
    }

    @Test
    fun `fetch analytics http error`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        assertFailsWith<DataFetchException> { MoexDataSource.fetchAnalytics() }

        server.shutdown()
    }

    @Test
    fun `fetch dividends`() = runBlocking {
        val server = MockWebServer()
        val body = javaClass.getResource("/dividends.json")!!.readText()
        server.enqueue(MockResponse().setBody(body))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        val dy = MoexDataSource.fetchDividends(200.0)

        server.shutdown()
        assertEquals(75.0, dy)
    }

    @Test
    fun `fetch dividends missing field`() = runBlocking {
        val server = MockWebServer()
        val body = javaClass.getResource("/dividends_missing.json")!!.readText()
        server.enqueue(MockResponse().setBody(body))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        assertFailsWith<DividendsParseException> { MoexDataSource.fetchDividends(200.0) }

        server.shutdown()
    }

    @Test
    fun `fetch dividends http error`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))

        assertFailsWith<DataFetchException> { MoexDataSource.fetchDividends(200.0) }

        server.shutdown()
    }

    @Test
    fun `fetch ofz yield`() = runBlocking {
        val server = MockWebServer()
        val body = javaClass.getResource("/zcyc.json")!!.readText()
        server.enqueue(MockResponse().setBody(body))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))
        val today = LocalDate.of(2025, 6, 6)

        val yld = MoexDataSource.fetchOfzYield(today)

        server.shutdown()
        assertEquals(15.3, yld)
    }

    @Test
    fun `fetch ofz yield missing field`() = runBlocking {
        val server = MockWebServer()
        val body = javaClass.getResource("/zcyc_missing.json")!!.readText()
        server.enqueue(MockResponse().setBody(body))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))
        val today = LocalDate.of(2025, 6, 6)

        assertFailsWith<OfzYieldException> { MoexDataSource.fetchOfzYield(today) }

        server.shutdown()
    }

    @Test
    fun `fetch ofz yield http error`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500))
        server.start()
        System.setProperty("moex.base", server.url("/").toString().removeSuffix("/"))
        val today = LocalDate.of(2025, 6, 6)

        assertFailsWith<DataFetchException> { MoexDataSource.fetchOfzYield(today) }

        server.shutdown()
    }
}
