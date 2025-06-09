import data.market.HttpCapeRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HttpCapeRepositoryTest {
    @Test
    fun `parse csv`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setBody(
                "observation_date,RUCAPE\n2024-01-31,5.0\n2024-02-29,6.2\n"
            )
        )
        server.start()
        val repo = HttpCapeRepository(OkHttpClient(), server.url("/").toString())
        val value = repo.fetchCape()
        server.shutdown()
        assertEquals(6.2, value)
    }

    @Test
    fun `error on invalid body`() = runBlocking {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("<html></html>"))
        server.start()
        val repo = HttpCapeRepository(OkHttpClient(), server.url("/").toString())
        assertFailsWith<IllegalArgumentException> { repo.fetchCape() }
        server.shutdown()
    }
}
