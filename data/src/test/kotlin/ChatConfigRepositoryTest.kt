import data.ChatConfig
import data.InMemoryChatConfigRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatConfigRepositoryTest {
    @Test
    fun `update and retrieve`() {
        val repo = InMemoryChatConfigRepository()
        val id = 123L
        val config = repo.getConfig(id)
        assertEquals(100_000.0, config.monthlyFlow)
        val updated = config.copy(monthlyFlow = 200_000.0)
        repo.update(id, updated)
        assertEquals(200_000.0, repo.getConfig(id).monthlyFlow)
    }

    @Test
    fun `allChatIds returns ids`() {
        val repo = InMemoryChatConfigRepository()
        val id = 1L
        repo.getConfig(id)
        assertEquals(listOf(id), repo.allChatIds())
    }
}
