import data.market.MarketData
import service.Strategy
import service.dto.Portfolio
import service.dto.StrategyConfig
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StrategyFilterTest {
    @Test
    fun `filters all passed`() {
        val md = MarketData(2800.0, 3000.0, 2700.0, 2760.0, 25.0, 6.0, 13.0, 10.0, 7.0)
        val p = Portfolio(1_000_000.0, 300_000.0, 300_000.0)
        val cfg = StrategyConfig()
        assertTrue(Strategy.passRiskFilters(md, p, cfg))
        val bad = md.copy(cape = 9.0)
        assertFalse(Strategy.passRiskFilters(bad, p, cfg))
    }
}
