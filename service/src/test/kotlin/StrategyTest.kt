import service.Strategy
import service.dto.StrategyConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class StrategyTest {
    private val config = StrategyConfig()

    @Test
    fun `level one tranche`() {
        val amount = Strategy.enhancedTranche(-15.0, 0.20, config)
        assertEquals(25000.0, amount, 1e-2)
    }

    @Test
    fun `level two adjusted by sigma`() {
        val amount = Strategy.enhancedTranche(-25.0, 0.30, config)
        assertEquals(33333.33, amount, 1.0)
    }

    @Test
    fun `clamped high tranche`() {
        val amount = Strategy.enhancedTranche(-25.0, 0.05, config)
        assertEquals(125000.0, amount, 1e-2)
    }

    @Test
    fun `no tranche above threshold`() {
        val amount = Strategy.enhancedTranche(-5.0, 0.20, config)
        assertEquals(0.0, amount, 1e-2)
    }
}

