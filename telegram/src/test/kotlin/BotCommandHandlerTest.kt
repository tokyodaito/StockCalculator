import bot.BotCommandHandler
import bot.TextResponse
import bot.WebAppResponse
import data.InMemoryChatConfigRepository
import data.macro.MacroData
import data.market.MarketData
import kotlinx.coroutines.runBlocking
import service.DcaService
import service.dto.Portfolio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BotCommandHandlerTest {
    @Test
    fun `set monthly flow updates config`() =
        runBlocking {
            val repo = InMemoryChatConfigRepository()
            val ds =
                DcaService({
                    MarketData(
                        price = 1.0,
                        max52 = 1.0,
                        sma200 = 1.0,
                        sma50 = 1.0,
                        rsi14 = 1.0,
                        sigma30 = 1.0,
                        cape = 1.0,
                    )
                }) { MacroData(70.0, 10.0, 9.5) }
            val handler = BotCommandHandler(repo, ds, "http://localhost")
            val portfolio = Portfolio(0.0, 0.0, 0.0)
            handler.handle(1, "/set_monthly_flow 150000", portfolio)
            assertEquals(150_000.0, repo.getConfig(1).monthlyFlow)
        }

    @Test
    fun `report now returns text`() =
        runBlocking {
            val repo = InMemoryChatConfigRepository()
            val md =
                MarketData(
                    price = 2650.0,
                    max52 = 3000.0,
                    sma200 = 2700.0,
                    sma50 = 2750.0,
                    rsi14 = 28.0,
                    sigma30 = 7.0,
                    cape = 7.0,
                )
            val ds = DcaService({ md }) { MacroData(70.0, 10.0, 9.5) }
            val handler = BotCommandHandler(repo, ds, "http://localhost")
            val portfolio = Portfolio(700_000.0, 300_000.0, 300_000.0)
            val text = handler.handle(1, "/report_now", portfolio) as TextResponse
            assertTrue(text.text.contains("price="))
        }

    @Test
    fun `open webapp returns webapp response`() =
        runBlocking {
            val repo = InMemoryChatConfigRepository()
            val md = MarketData(
                price = 2650.0,
                max52 = 3000.0,
                sma200 = 2700.0,
                sma50 = 2800.0,
                rsi14 = 28.0,
                sigma30 = 6.3,
                cape = 7.0,
            )
            val ds = DcaService({ md }) { MacroData(70.0, 10.0, 9.5) }
            val handler = BotCommandHandler(repo, ds, "http://localhost")
            val portfolio = Portfolio(0.0, 0.0, 0.0)
            val resp = handler.handle(1, "/open_webapp", portfolio)
            assertTrue(resp is WebAppResponse)
        }
}
