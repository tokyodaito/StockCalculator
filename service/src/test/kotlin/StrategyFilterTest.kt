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
        val md =
            MarketData(
                price = 2800.0,
                max52 = 3000.0,
                sma200 = 2700.0,
                sma50 = 2760.0,
                rsi14 = 25.0,
                sigma30 = 7.0,
                cape = 7.0,
            )
        val p = Portfolio(equity = 1_000_000.0, others = 300_000.0, cushionAmount = 300_000.0)
        val cfg = StrategyConfig()
        assertTrue(Strategy.passRiskFilters(marketData = md, portfolio = p, config = cfg))
        val bad = md.copy(cape = 9.0)
        assertFalse(Strategy.passRiskFilters(marketData = bad, portfolio = p, config = cfg))
    }
}
