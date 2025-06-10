import data.market.CapeRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CapeRepositoryTest {
    @Test
    fun `parse json`() =
        runBlocking {
            val server = MockWebServer()
            server.enqueue(
                MockResponse().setBody(
                    "[{'date':'2024-01-31','cape':5.0},{'date':'2024-02-29','cape':6.2}]".replace('\'', '"'),
                ),
            )
            server.start()
            val repo = CapeRepositoryImpl(OkHttpClient(), Json { ignoreUnknownKeys = true }, server.url("/").toString())
            val value = repo.fetchCape()
            server.shutdown()
            assertEquals(6.2, value)
        }

    @Test
    fun `error on invalid body`() =
        runBlocking {
            val server = MockWebServer()
            server.enqueue(MockResponse().setBody("<html></html>"))
            server.start()
            val repo = CapeRepositoryImpl(OkHttpClient(), Json { ignoreUnknownKeys = true }, server.url("/").toString())
            assertFailsWith<IllegalArgumentException> { repo.fetchCape() }
            server.shutdown()
        }
}
