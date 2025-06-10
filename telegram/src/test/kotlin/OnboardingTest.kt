import bot.BotCommandHandler
import bot.TextResponse
import data.InMemoryChatConfigRepository
import data.OnboardingStage
import data.macro.MacroData
import data.market.MarketData
import kotlinx.coroutines.runBlocking
import service.DcaService
import service.dto.Portfolio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingTest {
    private fun handler(): BotCommandHandler {
        val md = MarketData(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
        val ds = DcaService({ md }) { MacroData(1.0, 1.0, 1.0) }
        val repo = InMemoryChatConfigRepository()
        return BotCommandHandler(repo, ds, "http://localhost")
    }

    @Test
    fun `start sets state`() = runBlocking {
        val repo = InMemoryChatConfigRepository()
        val md = MarketData(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
        val ds = DcaService({ md }) { MacroData(1.0, 1.0, 1.0) }
        val handler = BotCommandHandler(repo, ds, "http://localhost")
        val p = Portfolio(0.0, 0.0, 0.0)
        val resp = handler.handle(1, "/start", p) as TextResponse
        assertTrue(resp.text.contains("бот"))
        assertEquals(OnboardingStage.WAIT_START, repo.getConfig(1).onboardingStage)
    }

    @Test
    fun `expenses step`() = runBlocking {
        val repo = InMemoryChatConfigRepository()
        val md = MarketData(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
        val ds = DcaService({ md }) { MacroData(1.0, 1.0, 1.0) }
        val handler = BotCommandHandler(repo, ds, "http://localhost")
        val p = Portfolio(0.0, 0.0, 0.0)
        handler.handle(1, "/start", p)
        val resp = handler.handle(1, "Начать", p) as TextResponse
        assertTrue(resp.text.contains("расходы"))
        assertEquals(OnboardingStage.WAIT_EXPENSES, repo.getConfig(1).onboardingStage)
        val resp2 = handler.handle(1, "50000", p) as TextResponse
        assertTrue(resp2.text.contains("подушка"))
        assertEquals(OnboardingStage.WAIT_CUSHION_CONFIRM, repo.getConfig(1).onboardingStage)
    }
}
