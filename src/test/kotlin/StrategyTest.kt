import org.example.Strategy
import data.market.MarketData
import org.example.Portfolio
import org.example.StrategyConfig
import org.example.Action
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrategyTest {
    @Test
    fun testDeltaCalculation() {
        val m = MarketData(price = 90.0, max52 = 100.0, sma200 = 0.0, rsi14 = 0.0, pe = 0.0, dy = 0.0, ofzYield = 0.0)
        assertEquals(-10.0, Strategy.delta(m))
    }

    @Test
    fun testEnhancedTrancheLevels() {
        assertEquals(0.0, Strategy.enhancedTranche(-5.0))
        assertEquals(StrategyConfig.BASE_DCA_AMOUNT, Strategy.enhancedTranche(-15.0))
        assertEquals(1.5 * StrategyConfig.BASE_DCA_AMOUNT, Strategy.enhancedTranche(-25.0))
        assertEquals(2.0 * StrategyConfig.BASE_DCA_AMOUNT, Strategy.enhancedTranche(-35.0))
    }

    @Test
    fun testEvaluateDca() {
        val market = MarketData(
            price = 2650.0,
            max52 = 3000.0,
            sma200 = 2700.0,
            rsi14 = 28.0,
            pe = 6.3,
            dy = 13.0,
            ofzYield = 10.5
        )
        val portfolio = Portfolio(
            equity = 700_000.0,
            others = 300_000.0,
            cushionAmount = 300_000.0
        )
        val actions = Strategy.evaluate(java.time.LocalDate.of(2025, 6, 10), market, portfolio)
        assertTrue(actions.any { it is Action.Dca })
    }
}
