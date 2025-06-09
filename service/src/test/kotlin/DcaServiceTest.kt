import data.market.MarketData
import data.macro.MacroData
import service.DcaService
import service.dto.Portfolio
import service.dto.StrategyConfig
import kotlin.test.Test
import kotlin.test.assertTrue

class DcaServiceTest {
    @Test
    fun `generate report with actions`() {
        val md = MarketData(2650.0, 3000.0, 2700.0, 28.0, 6.3, 13.0, 10.5, 0.2)
        val macro = MacroData(brent = 80.0, keyRate = 10.0, keyRate6mAgo = 11.0)
        val ds = DcaService({ md }) { macro }
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
