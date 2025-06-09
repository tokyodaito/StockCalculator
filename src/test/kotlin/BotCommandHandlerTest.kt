import bot.BotCommandHandler
import bot.DcaService
import bot.InMemoryChatConfigRepository
import kotlinx.coroutines.runBlocking
import data.market.MarketData
import org.example.Portfolio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BotCommandHandlerTest {
    @Test
    fun `set monthly flow updates config`() = runBlocking {
        val repo = InMemoryChatConfigRepository()
        val ds = DcaService { MarketData(1.0,1.0,1.0,1.0,1.0,1.0,1.0) }
        val handler = BotCommandHandler(repo, ds)
        val portfolio = Portfolio(0.0,0.0,0.0)
        handler.handle(1, "/set_monthly_flow 150000", portfolio)
        assertEquals(150_000.0, repo.getConfig(1).monthlyFlow)
    }

    @Test
    fun `report now returns text`() = runBlocking {
        val repo = InMemoryChatConfigRepository()
        val md = MarketData(2650.0, 3000.0, 2700.0, 28.0, 6.3, 13.0, 10.5)
        val ds = DcaService { md }
        val handler = BotCommandHandler(repo, ds)
        val portfolio = Portfolio(700_000.0, 300_000.0, 300_000.0)
        val text = handler.handle(1, "/report_now", portfolio)
        assertTrue(text.contains("price="))
    }
}
