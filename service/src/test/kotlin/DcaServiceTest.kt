import data.macro.MacroData
import data.market.MarketData
import service.DcaService
import service.dto.Portfolio
import service.dto.StrategyConfig
import kotlin.test.Test
import kotlin.test.assertTrue

class DcaServiceTest {
    @Test
    fun `generate report with actions`() {
        val md =
            MarketData(
                price = 2650.0,
                max52 = 3000.0,
                sma200 = 2700.0,
                sma50 = 2750.0,
                rsi14 = 28.0,
                dy = 13.0,
                ofzYield = 10.5,
                cape = 7.0,
            )
        val ds = DcaService { md }
        val macro = MacroData(brent = 80.0, keyRate = 10.0, keyRate6mAgo = 11.0)
        val portfolio = Portfolio(700_000.0, 300_000.0, 300_000.0)
        val config = StrategyConfig()
        val text =
            kotlinx.coroutines.runBlocking {
                ds.generateReport(java.time.LocalDate.of(2025, 6, 10), portfolio, config)
            }
        assertTrue(text.contains("DCA"))
        assertTrue(text.contains("Коридор акций"))
    }
}
