import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import data.market.MarketDataSerializer

class MarketDataSerializerTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun loadHistory(): String =
        javaClass.getResource("/history.json")!!.readText()

    @Test
    fun parsePage() {
        val root = json.parseToJsonElement(loadHistory()).jsonObject
        val page = MarketDataSerializer.parsePage(root)
        assertEquals(256, page.closes.size)
        assertEquals(3192.38, page.closes.first())
        assertEquals(3215.99, page.highs.first())
        assertEquals(256, page.total)
        assertEquals(256, page.pageSize)
    }

    @Test
    fun toMarketData() {
        val root = json.parseToJsonElement(loadHistory()).jsonObject
        val page = MarketDataSerializer.parsePage(root)
        val data = MarketDataSerializer.toMarketData(page.closes, page.highs)
        assertEquals(2786.16, data.price)
        assertEquals(3371.06, data.max52)
        assertEquals(2835.10805, data.sma200, 0.0001)
        assertEquals(43.85039, data.rsi14, 0.0001)
    }

    @Test
    fun `toMarketData fails when too few closes`() {
        val closes = List(199) { 1.0 }
        val highs = List(199) { 1.0 }
        assertFailsWith<IllegalArgumentException> {
            MarketDataSerializer.toMarketData(closes, highs)
        }
    }
}
